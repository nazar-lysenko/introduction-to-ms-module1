package com.storageservice.storage;

import com.storageservice.storage.dto.CreateStorageRequest;
import com.storageservice.storage.dto.StorageCreatedDto;
import com.storageservice.storage.dto.StorageDeletedDto;
import com.storageservice.storage.validation.ValidCsvIds;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/storages")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    @PostMapping
    public ResponseEntity<StorageCreatedDto> createStorage(@RequestBody @Valid CreateStorageRequest request) {
        return ResponseEntity.ok(storageService.createStorage(request));
    }

    @GetMapping
    public ResponseEntity<List<Storage>> getAllStorages() {
        return ResponseEntity.ok(storageService.getAllStorages());
    }

    @DeleteMapping
    public ResponseEntity<StorageDeletedDto> deleteStorages(@RequestParam @ValidCsvIds String id) {
        return ResponseEntity.ok(storageService.deleteStorages(id));
    }
}
