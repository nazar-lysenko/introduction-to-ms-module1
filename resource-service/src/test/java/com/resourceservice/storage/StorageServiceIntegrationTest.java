package com.resourceservice.storage;

import com.resourceservice.config.ApplicationConfiguration;
import com.resourceservice.config.S3Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = {
        StorageService.class,
        ApplicationConfiguration.class
})
@EnableConfigurationProperties(S3Properties.class)
@ActiveProfiles("test")
@Testcontainers
class StorageServiceIntegrationTest {

    @Container
    static LocalStackContainer localStack =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3"))
                    .withServices(LocalStackContainer.Service.S3)
                    .waitingFor(Wait.forHttp("/_localstack/health")
                            .forPort(4566)
                            .forStatusCode(200));

    private static final String BUCKET_NAME = "test-bucket";
    private static final byte[] TEST_DATA = "mp3 content".getBytes();
    private static final byte[] TEST_DATA_1 = "first mp3".getBytes();
    private static final byte[] TEST_DATA_2 = "second mp3".getBytes();
    private static final String EXPECTED_KEY_PREFIX = "resources/";
    private static final String EXPECTED_KEY_SUFFIX = ".mp3";

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.s3.endpoint", () -> localStack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        registry.add("aws.s3.region", localStack::getRegion);
        registry.add("aws.s3.access-key", localStack::getAccessKey);
        registry.add("aws.s3.secret-key", localStack::getSecretKey);
    }

    @BeforeAll
    static void createBucket() {
        S3Client s3 = S3Client.builder()
                .endpointOverride(localStack.getEndpointOverride(LocalStackContainer.Service.S3))
                .region(Region.of(localStack.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(localStack.getAccessKey(), localStack.getSecretKey())))
                .forcePathStyle(true)
                .build();
        s3.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
        s3.close();
    }

    @Autowired
    private StorageService storageService;

    @Test
    void shouldUploadAndDownload() {
        String key = storageService.upload(TEST_DATA);

        assertThat(key).startsWith(EXPECTED_KEY_PREFIX).endsWith(EXPECTED_KEY_SUFFIX);
        assertThat(storageService.download(key)).isEqualTo(TEST_DATA);
    }

    @Test
    void shouldDeleteUploadedObject() {
        String key = storageService.upload(TEST_DATA);

        storageService.delete(key);

        assertThrows(NoSuchKeyException.class, () -> storageService.download(key));
    }

    @Test
    void shouldUploadMultipleObjectsWithUniqueKeys() {
        String key1 = storageService.upload(TEST_DATA_1);
        String key2 = storageService.upload(TEST_DATA_2);

        assertThat(key1).isNotEqualTo(key2);
        assertThat(storageService.download(key1)).isEqualTo(TEST_DATA_1);
        assertThat(storageService.download(key2)).isEqualTo(TEST_DATA_2);
    }

}