package com.storageservice.storage.dto;

import com.storageservice.storage.StorageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateStorageRequest {

    @NotNull
    private StorageType storageType;

    @NotBlank
    private String bucket;

    @NotBlank
    private String path;
}
