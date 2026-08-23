package com.resourceservice.resource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.resourceservice.storage.StorageType;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {ResourceRepositoryIntegrationTest.JpaConfig.class})
@ActiveProfiles("test")
@Testcontainers
class ResourceRepositoryIntegrationTest {

    private static final String STORAGE_PATH_1 = "files/r1.mp3";
    private static final String STORAGE_PATH_2 = "files/r2.mp3";
    private static final String STORAGE_PATH_3 = "files/r3.mp3";
    private static final String BUCKET = "staging-bucket";
    private static final long NON_EXISTING_ID = 9999L;

    private Resource buildResource(String storagePath) {
        Resource resource = new Resource();
        resource.setStoragePath(storagePath);
        resource.setStorageBucket(BUCKET);
        resource.setStorageType(StorageType.STAGING);
        return resource;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EnableJpaRepositories(basePackageClasses = ResourceRepository.class)
    @EntityScan(basePackageClasses = Resource.class)
    static class JpaConfig {
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine")
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ResourceRepository repository;

    @Test
    void shouldPersistResourceAndGenerateId() {
        Resource resource = buildResource(STORAGE_PATH_1);

        Resource saved = repository.save(resource);

        assertThat(saved.getId()).isNotNull().isGreaterThan(0L);
        assertThat(saved.getStoragePath()).isEqualTo(STORAGE_PATH_1);
    }

    @Test
    void shouldFindResourceById() {
        Resource resource = buildResource(STORAGE_PATH_1);
        Resource saved = repository.save(resource);

        Optional<Resource> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStoragePath()).isEqualTo(STORAGE_PATH_1);
    }

    @Test
    void shouldReturnEmptyForNonExistingId() {
        Optional<Resource> found = repository.findById(NON_EXISTING_ID);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllByIdsReturnsOnlyMatchingEntities() {
        Resource r1 = buildResource(STORAGE_PATH_1);
        Resource r2 = buildResource(STORAGE_PATH_2);
        Resource r3 = buildResource(STORAGE_PATH_3);
        repository.saveAll(List.of(r1, r2, r3));

        List<Resource> found = repository.findAllById(List.of(r1.getId(), r3.getId()));

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Resource::getStoragePath)
                .containsExactlyInAnyOrder(STORAGE_PATH_1, STORAGE_PATH_3);
    }

    @Test
    void shouldDeleteResourceById() {
        Resource resource = buildResource(STORAGE_PATH_1);
        Resource saved = repository.save(resource);

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    @Transactional
    void shouldDeleteMultipleResourcesByCustomQuery() {
        Resource r1 = buildResource(STORAGE_PATH_1);
        Resource r2 = buildResource(STORAGE_PATH_2);
        Resource r3 = buildResource(STORAGE_PATH_3);
        repository.saveAll(List.of(r1, r2, r3));

        repository.deleteByIds(List.of(r1.getId(), r2.getId()));

        assertThat(repository.findById(r1.getId())).isEmpty();
        assertThat(repository.findById(r2.getId())).isEmpty();
        assertThat(repository.findById(r3.getId())).isPresent();
    }

    @Test
    @Transactional
    void shouldIgnoreNonExistingIdsInDeleteByIds() {
        Resource resource = buildResource(STORAGE_PATH_1);
        Resource saved = repository.save(resource);

        repository.deleteByIds(List.of(saved.getId(), NON_EXISTING_ID));

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}
