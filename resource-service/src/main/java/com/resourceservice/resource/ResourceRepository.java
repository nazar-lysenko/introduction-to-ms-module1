package com.resourceservice.resource;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Resource r WHERE r.id IN :ids")
    void deleteByIds(@Param("ids") List<Long> ids);
}
