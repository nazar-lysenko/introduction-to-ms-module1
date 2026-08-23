package com.resourceservice.client;

import com.resourceservice.config.CircuitBreakerConfiguration;
import com.resourceservice.config.StorageServiceProperties;
import com.resourceservice.storage.StorageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageServiceClient {

    static final List<StorageDto> FALLBACK_STORAGES = List.of(
            new StorageDto(1L, StorageType.STAGING, "staging-bucket", "/files"),
            new StorageDto(2L, StorageType.PERMANENT, "permanent-bucket", "/files")
    );

    private static final String STORAGES_PATH = "/storages";

    private final RestTemplate restTemplate;
    private final StorageServiceProperties storageServiceProperties;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public List<StorageDto> getAllStorages() {
        return circuitBreakerFactory.create(CircuitBreakerConfiguration.STORAGE_SERVICE_CB).run(
                () -> {
                    log.debug("Fetching all storages from storage-service");
                    return restTemplate.exchange(
                            storageServiceProperties.getUrl() + STORAGES_PATH,
                            HttpMethod.GET,
                            null,
                            new ParameterizedTypeReference<List<StorageDto>>() {}
                    ).getBody();
                },
                throwable -> {
                    log.warn("Storage service unavailable ({}), returning stub fallback data", throwable.getMessage());
                    return FALLBACK_STORAGES;
                }
        );
    }

    public StorageDto getStorageByType(StorageType type) {
        List<StorageDto> storages = getAllStorages();

        if (storages == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Storage service returned no data");
        }

        return storages.stream()
                .filter(s -> type == s.storageType())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No storage of type " + type + " found"));
    }
}
