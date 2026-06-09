package com.resourceservice.resource;

import com.resourceservice.client.SongServiceClient;
import com.resourceservice.metadata.ResourceMetadata;
import com.resourceservice.metadata.ResourceMetadataExtractorService;
import com.resourceservice.resource.dto.ResourceCreatedDto;
import com.resourceservice.resource.dto.ResourceDeletedDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResourceService {
    private static final String CSV_ID_SEPARATOR = ",";

    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;
    private final ResourceMetadataExtractorService metadataExtractorService;
    private final SongServiceClient songServiceClient;

    @Transactional
    public ResourceCreatedDto createResource(byte[] data) {
        Resource resource = new Resource();
        resource.setData(data);
        resource = resourceRepository.save(resource);

        ResourceMetadata metadata = metadataExtractorService.extract(resource.getId(), data);
        songServiceClient.createSongMetadata(metadata);

        return resourceMapper.toCreatedDto(resource);
    }

    public byte[] getResource(Long id) {
        return resourceRepository.findById(id)
                .map(Resource::getData)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource with ID=%d not found".formatted(id)));
    }

    @Transactional
    public ResourceDeletedDto deleteResources(String idsParam) {
        List<Long> ids = Arrays.stream(idsParam.split(CSV_ID_SEPARATOR))
                .map(Long::parseLong)
                .toList();

        List<Resource> existingResources = resourceRepository.findAllById(ids);
        List<Long> idsToDelete = existingResources.stream()
                .map(Resource::getId)
                .toList();

        songServiceClient.deleteSongMetadata(ids);
        resourceRepository.deleteAllById(idsToDelete);

        return new ResourceDeletedDto(idsToDelete);
    }
}
