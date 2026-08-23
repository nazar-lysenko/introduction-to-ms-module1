package com.storageservice.storage;

import com.storageservice.storage.dto.CreateStorageRequest;
import com.storageservice.storage.dto.StorageCreatedDto;
import com.storageservice.storage.dto.StorageDeletedDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {

    private static final String CSV_SEPARATOR = ",";

    private final StorageRepository storageRepository;

    public StorageCreatedDto createStorage(CreateStorageRequest request) {
        Storage storage = new Storage();
        storage.setStorageType(request.getStorageType());
        storage.setBucket(request.getBucket());
        storage.setPath(request.getPath());
        storage = storageRepository.save(storage);

        return new StorageCreatedDto(storage.getId());
    }

    public List<Storage> getAllStorages() {
        return storageRepository.findAll();
    }

    @Transactional
    public StorageDeletedDto deleteStorages(String idsParam) {
        List<Long> ids = Arrays.stream(idsParam.split(CSV_SEPARATOR))
                .map(String::trim)
                .map(Long::parseLong)
                .toList();

        List<Long> existingIds = storageRepository.findAllById(ids)
                .stream()
                .map(Storage::getId)
                .toList();

        if (!existingIds.isEmpty()) {
            storageRepository.deleteByIds(existingIds);
        }

        return new StorageDeletedDto(existingIds);
    }
}
