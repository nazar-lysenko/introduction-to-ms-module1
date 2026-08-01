package com.resourceservice.resource;

import com.resourceservice.client.StorageDto;
import com.resourceservice.client.StorageServiceClient;
import com.resourceservice.client.SongServiceClient;
import com.resourceservice.messaging.ResourceEventPublisher;
import com.resourceservice.resource.dto.ResourceCreatedDto;
import com.resourceservice.resource.dto.ResourceDeletedDto;
import com.resourceservice.storage.StorageService;
import com.resourceservice.storage.StorageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceService {
    private static final String CSV_ID_SEPARATOR = ",";

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final SongServiceClient songServiceClient;
    private final StorageService storageService;
    private final StorageServiceClient storageServiceClient;
    private final ResourceEventPublisher resourceEventPublisher;

    @Transactional
    public ResourceCreatedDto createResource(byte[] data) {
        StorageDto staging = storageServiceClient.getStorageByType(StorageType.STAGING);

        String storagePath = storageService.upload(data, staging.bucket(), staging.path());
        Resource resource = new Resource();

        try {
            resource.setStoragePath(storagePath);
            resource.setStorageBucket(staging.bucket());
            resource.setStorageType(StorageType.STAGING);
            resource = resourceRepository.save(resource);
        } catch (DataAccessException ex) {
            log.error("Can not save resource", ex);
            storageService.delete(staging.bucket(), storagePath);
            throw ex;
        }

        resourceEventPublisher.publishResourceCreated(resource.getId());

        return resourceMapper.toCreatedDto(resource);
    }

    public byte[] getResource(Long id) {
        return resourceRepository.findById(id)
                .map(r -> storageService.download(r.getStorageBucket(), r.getStoragePath()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Resource with ID=%d not found".formatted(id)));
    }

    @Transactional
    public ResourceDeletedDto deleteResources(String idsParam) {
        List<Long> ids = Arrays.stream(idsParam.split(CSV_ID_SEPARATOR))
                .map(Long::parseLong)
                .toList();

        List<Resource> existingResources = resourceRepository.findAllById(ids);
        List<Long> existingIds = existingResources.stream().map(Resource::getId).toList();

        if (existingIds.isEmpty()) {
            return new ResourceDeletedDto(existingIds);
        }

        resourceRepository.deleteByIds(existingIds);
        songServiceClient.deleteSongMetadata(existingIds);
        existingResources.forEach(r -> storageService.delete(r.getStorageBucket(), r.getStoragePath()));

        return new ResourceDeletedDto(existingIds);
    }

    @Transactional
    public void promoteResourceToPermanent(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Resource not found: " + resourceId));

        if (resource.getStorageType() == StorageType.PERMANENT) {
            log.info("Resource id={} already in PERMANENT storage, skipping", resourceId);
            return;
        }

        StorageDto permanent = storageServiceClient.getStorageByType(StorageType.PERMANENT);

        String newKey = storageService.move(
                resource.getStorageBucket(),
                resource.getStoragePath(),
                permanent.bucket(),
                permanent.path()
        );

        resource.setStoragePath(newKey);
        resource.setStorageBucket(permanent.bucket());
        resource.setStorageType(StorageType.PERMANENT);
        resourceRepository.save(resource);

        log.info("Promoted resource id={} to PERMANENT storage (bucket={}, key={})",
                resourceId, permanent.bucket(), newKey);
    }
}
