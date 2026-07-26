package com.resourceservice.component.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.resourceservice.component.ComponentTestConfiguration;
import com.resourceservice.config.S3Properties;
import com.resourceservice.resource.Resource;
import com.resourceservice.resource.ResourceRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ResourceManagementSteps {

    private static final byte[] FAKE_MP3_BYTES = new byte[]{
            0x49, 0x44, 0x33, 0x04, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x54, 0x45, 0x53, 0x54, 0x00, 0x00,
            (byte) 0xFF, (byte) 0xFB, (byte) 0x90, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    private static final byte[] TEST_INVALID_FILE_CONTENT = "Not an MP3 file content".getBytes();
    private static final MediaType AUDIO_MPEG = MediaType.parseMediaType("audio/mpeg");
    private static final String SONG_SERVICE_RECOVERY_SCENARIO = "song-service-recovery";
    private static final String SCENARIO_STATE_FAILING_1 = "FAILING_1";
    private static final String SCENARIO_STATE_FAILING_2 = "FAILING_2";
    private static final String SCENARIO_STATE_RECOVERED = "RECOVERED";
    private static final Duration KAFKA_POLL_INTERVAL = Duration.ofMillis(500);
    private static final Duration KAFKA_EVENT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration KAFKA_DRAIN_WINDOW = Duration.ofSeconds(3);
    private static final long DB_AWAIT_TIMEOUT_SECONDS = 5L;
    private static final long WIREMOCK_AWAIT_TIMEOUT_SECONDS = 10L;

    private static KafkaConsumer<String, Long> testConsumer;

    @Value("${kafka.topic.resource-events}")
    private String kafkaTopic;

    private KafkaConsumer<String, Long> getTestConsumer() {
        if (testConsumer == null) {
            testConsumer = new KafkaConsumer<>(Map.of(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ComponentTestConfiguration.kafka.getBootstrapServers(),
                    ConsumerConfig.GROUP_ID_CONFIG, "component-test-consumer-" + UUID.randomUUID(),
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName(),
                    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName(),
                    ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true",
                    ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100"
            ));
            testConsumer.subscribe(List.of(kafkaTopic));
        }
        return testConsumer;
    }

    @LocalServerPort
    private int port;

    private final WireMockServer wireMockServer = ComponentTestConfiguration.wireMockServer;

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Properties s3Properties;

    private final RestTemplate restTemplate = new RestTemplate();

    private byte[] requestPayload;
    private MediaType requestContentType;
    private ResponseEntity<?> lastResponse;
    private Long storedResourceId;
    private String storedS3Path;

    @Before
    public void resetState() {
        wireMockServer.resetAll();
        resourceRepository.deleteAll();
        getTestConsumer().poll(KAFKA_POLL_INTERVAL);
        requestPayload = null;
        requestContentType = null;
        lastResponse = null;
        storedResourceId = null;
        storedS3Path = null;
    }

    @Given("the resource-service is running")
    public void theResourceServiceIsRunning() {
    }

    @And("the S3 bucket {string} exists")
    public void theS3BucketExists(String bucket) {
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        } catch (BucketAlreadyOwnedByYouException | BucketAlreadyExistsException ignored) {
        }
    }

    @And("the song-service is available")
    public void theSongServiceIsAvailable() {
        wireMockServer.stubFor(delete(urlMatching("/songs.*"))
                .willReturn(aResponse().withStatus(200)));
    }

    @Given("I have a valid MP3 file {string}")
    public void iHaveAValidMp3File(String filename) {
        requestPayload = FAKE_MP3_BYTES;
        requestContentType = AUDIO_MPEG;
    }

    @Given("I have a file {string} that is not an MP3")
    public void iHaveAFileThatIsNotAnMp3(String filename) {
        requestPayload = TEST_INVALID_FILE_CONTENT;
        requestContentType = MediaType.APPLICATION_PDF;
    }

    @Given("a resource exists in the database with its MP3 file in S3")
    public void aResourceExistsInTheDatabaseWithItsMp3FileInS3() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(AUDIO_MPEG);
        HttpEntity<byte[]> request = new HttpEntity<>(FAKE_MP3_BYTES, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl() + "/resources",
                HttpMethod.POST,
                request,
                Map.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        storedResourceId = ((Number) response.getBody().get("id")).longValue();

        Optional<Resource> resource = resourceRepository.findById(storedResourceId);
        assertThat(resource).isPresent();
        storedS3Path = resource.get().getStoragePath();
    }

    @And("the song-service will successfully delete songs for the resource")
    public void theSongServiceWillSuccessfullyDeleteSongsForTheResource() {
        wireMockServer.stubFor(delete(urlMatching("/songs.*"))
                .willReturn(aResponse().withStatus(200)));
    }

    @And("the song-service is temporarily unavailable but recovers after 2 attempts")
    public void theSongServiceIsTemporarilyUnavailableButRecoversAfter2Attempts() {
        wireMockServer.stubFor(delete(urlMatching("/songs.*"))
                .inScenario(SONG_SERVICE_RECOVERY_SCENARIO)
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo(SCENARIO_STATE_FAILING_1));

        wireMockServer.stubFor(delete(urlMatching("/songs.*"))
                .inScenario(SONG_SERVICE_RECOVERY_SCENARIO)
                .whenScenarioStateIs(SCENARIO_STATE_FAILING_1)
                .willReturn(aResponse().withStatus(503))
                .willSetStateTo(SCENARIO_STATE_FAILING_2));

        wireMockServer.stubFor(delete(urlMatching("/songs.*"))
                .inScenario(SONG_SERVICE_RECOVERY_SCENARIO)
                .whenScenarioStateIs(SCENARIO_STATE_FAILING_2)
                .willReturn(aResponse().withStatus(200))
                .willSetStateTo(SCENARIO_STATE_RECOVERED));
    }

    @When("I upload the MP3 file to the resource-service")
    public void iUploadTheMp3FileToTheResourceService() {
        performUpload();
    }

    @When("I upload the file to the resource-service")
    public void iUploadTheFileToTheResourceService() {
        performUpload();
    }

    @When("I request the stored resource by ID")
    public void iRequestTheStoredResourceById() {
        try {
            lastResponse = restTemplate.getForEntity(
                    baseUrl() + "/resources/" + storedResourceId,
                    byte[].class
            );
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            lastResponse = ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    @When("I request the resource with ID {long}")
    public void iRequestTheResourceWithId(long id) {
        try {
            lastResponse = restTemplate.getForEntity(
                    baseUrl() + "/resources/" + id,
                    byte[].class
            );
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            lastResponse = ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    @When("I delete the stored resource by ID")
    public void iDeleteTheStoredResourceById() {
        try {
            lastResponse = restTemplate.exchange(
                    baseUrl() + "/resources?id=" + storedResourceId,
                    HttpMethod.DELETE,
                    null,
                    Map.class
            );
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            lastResponse = ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatus) {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(expectedStatus);
    }

    @And("the response should contain the resource ID")
    public void theResponseShouldContainTheResourceId() {
        assertThat(lastResponse.getBody()).isNotNull();
        Map<?, ?> body = (Map<?, ?>) lastResponse.getBody();
        Number id = (Number) body.get("id");
        assertThat(id).isNotNull();
        assertThat(id.longValue()).isGreaterThan(0L);
        storedResourceId = id.longValue();
    }

    @And("the MP3 file should be stored in S3")
    public void theMp3FileShouldBeStoredInS3() {
        Resource resource = resourceRepository.findById(storedResourceId)
                .orElseThrow(() -> new AssertionError("Resource not found in DB"));
        storedS3Path = resource.getStoragePath();
        assertS3ObjectExists(storedS3Path);
    }

    @And("a resource record should be saved in the database")
    public void aResourceRecordShouldBeSavedInTheDatabase() {
        assertThat(resourceRepository.findById(storedResourceId)).isPresent();
    }

    @And("a resource event should be published to Kafka with the resource ID")
    public void aResourceEventShouldBePublishedToKafkaWithTheResourceId() {
        Long received = pollForKafkaEvent(storedResourceId, KAFKA_EVENT_TIMEOUT);
        assertThat(received)
                .as("Expected Kafka event for resourceId=%d", storedResourceId)
                .isNotNull()
                .isEqualTo(storedResourceId);
    }

    @And("no file should be stored in S3")
    public void noFileShouldBeStoredInS3() {
        assertThat(lastResponse.getStatusCode().value()).isEqualTo(400);
        assertThat(resourceRepository.count()).isZero();
    }

    @And("no resource record should be saved in the database")
    public void noResourceRecordShouldBeSavedInTheDatabase() {
        assertThat(resourceRepository.count()).isZero();
    }

    @And("no Kafka event should be published")
    public void noKafkaEventShouldBePublished() {
        List<Long> events = drainKafkaEvents(KAFKA_DRAIN_WINDOW);
        assertThat(events).as("Expected no Kafka events to be published").isEmpty();
    }

    @And("the response content type should be {string}")
    public void theResponseContentTypeShouldBe(String expectedContentType) {
        MediaType actual = lastResponse.getHeaders().getContentType();
        assertThat(actual).isNotNull();
        assertThat(actual.isCompatibleWith(MediaType.parseMediaType(expectedContentType))).isTrue();
    }

    @And("the response body should contain the original MP3 binary")
    public void theResponseBodyShouldContainTheOriginalMp3Binary() {
        byte[] body = (byte[]) lastResponse.getBody();
        assertThat(body).isNotNull().isNotEmpty();
        assertThat(body).isEqualTo(FAKE_MP3_BYTES);
    }

    @And("the resource record should be removed from the database")
    public void theResourceRecordShouldBeRemovedFromTheDatabase() {
        await().atMost(DB_AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(resourceRepository.findById(storedResourceId)).isEmpty()
        );
    }

    @And("the MP3 file should be removed from S3")
    public void theMp3FileShouldBeRemovedFromS3() {
        assertS3ObjectAbsent(storedS3Path);
    }

    @And("the song-service should have received a delete request")
    public void theSongServiceShouldHaveReceivedADeleteRequest() {
        wireMockServer.verify(1, deleteRequestedFor(urlMatching("/songs.*")));
    }

    @And("the song-service should have received {int} requests due to retries")
    public void theSongServiceShouldHaveReceivedRequestsDueToRetries(int expectedCount) {
        await().atMost(WIREMOCK_AWAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilAsserted(() ->
                wireMockServer.verify(expectedCount, deleteRequestedFor(urlMatching("/songs.*")))
        );
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private void performUpload() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(requestContentType);
        HttpEntity<byte[]> request = new HttpEntity<>(requestPayload, headers);

        try {
            lastResponse = restTemplate.exchange(
                    baseUrl() + "/resources",
                    HttpMethod.POST,
                    request,
                    Map.class
            );
        } catch (HttpClientErrorException ex) {
            lastResponse = ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    private Long pollForKafkaEvent(Long expectedId, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        KafkaConsumer<String, Long> consumer = getTestConsumer();
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, Long> records = consumer.poll(KAFKA_POLL_INTERVAL);
            for (ConsumerRecord<String, Long> record : records) {
                if (expectedId.equals(record.value())) {
                    return record.value();
                }
            }
        }
        return null;
    }

    private List<Long> drainKafkaEvents(Duration window) {
        List<Long> collected = new ArrayList<>();
        long deadline = System.currentTimeMillis() + window.toMillis();
        KafkaConsumer<String, Long> consumer = getTestConsumer();
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, Long> records = consumer.poll(KAFKA_POLL_INTERVAL);
            for (ConsumerRecord<String, Long> record : records) {
                collected.add(record.value());
            }
        }
        return collected;
    }

    private void assertS3ObjectExists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key)
                    .build());
        } catch (NoSuchKeyException e) {
            throw new AssertionError("Expected S3 object to exist but not found: " + key);
        }
    }

    private void assertS3ObjectAbsent(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(key)
                    .build());
            throw new AssertionError("Expected S3 object to be absent but it exists: " + key);
        } catch (NoSuchKeyException ignored) {
        }
    }
}
