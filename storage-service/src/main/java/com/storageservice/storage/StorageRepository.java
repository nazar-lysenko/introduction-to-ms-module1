package com.storageservice.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StorageRepository extends JpaRepository<Storage, Long> {

    Optional<Storage> findByStorageType(StorageType storageType);

    @Modifying
    @Query("DELETE FROM Storage s WHERE s.id IN :ids")
    void deleteByIds(List<Long> ids);
}
