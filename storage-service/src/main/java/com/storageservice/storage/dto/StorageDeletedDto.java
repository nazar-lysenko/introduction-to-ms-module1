package com.storageservice.storage.dto;

import java.util.List;

public record StorageDeletedDto(List<Long> ids) {
}
