package com.resourceservice.resource;

import com.resourceservice.resource.dto.ResourceCreatedDto;
import com.resourceservice.resource.dto.ResourceDeletedDto;
import com.resourceservice.resource.validation.ValidCsvIds;
import com.resourceservice.resource.validation.ValidResourceId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {
    private final ResourceService resourceService;

    @PostMapping(consumes = Constants.RESOURCE_SUPPORTED_MEDIA_TYPE)
    public ResponseEntity<ResourceCreatedDto> uploadResource(@RequestBody byte[] data) {
        return ResponseEntity.ok(resourceService.createResource(data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getResource(@PathVariable @ValidResourceId Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(Constants.RESOURCE_SUPPORTED_MEDIA_TYPE))
                .body(resourceService.getResource(id));
    }

    @DeleteMapping
    public ResponseEntity<ResourceDeletedDto> deleteResources(@RequestParam @ValidCsvIds String id) {
        return ResponseEntity.ok(resourceService.deleteResources(id));
    }
}
