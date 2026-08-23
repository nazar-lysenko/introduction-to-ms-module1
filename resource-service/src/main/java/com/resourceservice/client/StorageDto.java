package com.resourceservice.client;

import com.resourceservice.storage.StorageType;

public record StorageDto(Long id, StorageType storageType, String bucket, String path) {
}
