package com.resourceservice.resource;

import com.resourceservice.client.SongServiceClient;
import com.resourceservice.messaging.ResourceEventPublisher;
import com.resourceservice.resource.dto.ResourceCreatedDto;
import com.resourceservice.resource.dto.ResourceDeletedDto;
import com.resourceservice.storage.StorageService;
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
    private final ResourceEventPublisher resourceEventPublisher;

    @Transactional
    public ResourceCreatedDto createResource(byte[] data) {
        String storagePath = storageService.upload(data);
        Resource resource = new Resource();

        try {
            resource.setStoragePath(storagePath);
            resource = resourceRepository.save(resource);
        } catch (DataAccessException ex) {
            log.error("Can not save resource", ex);
            storageService.delete(storagePath);
            throw ex;
        }

        resourceEventPublisher.publishResourceCreated(resource.getId());

        return resourceMapper.toCreatedDto(resource);
    }

    public byte[] getResource(Long id) {
        return resourceRepository.findById(id)
                .map(Resource::getStoragePath)
                .map(storageService::download)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource with ID=%d not found".formatted(id)));
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

        List<String> existingPaths = existingResources.stream().map(Resource::getStoragePath).toList();

        resourceRepository.deleteByIds(existingIds);
        songServiceClient.deleteSongMetadata(existingIds);
        existingPaths.forEach(storageService::delete);

        return new ResourceDeletedDto(existingIds);
    }
}
