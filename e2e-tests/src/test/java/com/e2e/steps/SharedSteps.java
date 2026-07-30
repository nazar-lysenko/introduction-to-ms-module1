package com.e2e.steps;

import com.e2e.E2EScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SharedSteps {

    private static final String TEST_MP3_FILE_PATH = "files/test-song.mp3";
    private static final String AUDIO_MPEG_CONTENT_TYPE = "audio/mpeg";

    static final byte[] MINIMAL_MP3_BYTES;
    static {
        try (InputStream is = SharedSteps.class.getClassLoader().getResourceAsStream(TEST_MP3_FILE_PATH)) {
            if (is == null) {
                throw new IllegalStateException(TEST_MP3_FILE_PATH + " not found on classpath");
            }
            MINIMAL_MP3_BYTES = is.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + TEST_MP3_FILE_PATH, e);
        }
    }

    @Autowired
    private E2EScenarioContext context;

    @Value("${e2e.processing.timeout-seconds}")
    private int processingTimeoutSeconds;

    @Value("${e2e.processing.poll-interval-seconds}")
    private int processingPollIntervalSeconds;

    @Given("the system is fully operational")
    public void theSystemIsFullyOperational() {
    }

    @And("the API gateway is accessible at {string}")
    public void theApiGatewayIsAccessibleAt(String url) {
    }

    @And("a resource has been uploaded and processed successfully")
    public void aResourceHasBeenUploadedAndProcessedSuccessfully() {
        uploadAndWaitForProcessing();
    }

    @Given("a resource has been uploaded and its metadata has been processed")
    public void aResourceHasBeenUploadedAndItsMetadataHasBeenProcessed() {
        uploadAndWaitForProcessing();
    }

    void uploadAndWaitForProcessing() {
        context.setUploadedFileBytes(MINIMAL_MP3_BYTES);

        Response uploadResponse = given()
                .contentType(AUDIO_MPEG_CONTENT_TYPE)
                .body(MINIMAL_MP3_BYTES)
                .post("/resources");

        assertThat(uploadResponse.statusCode()).isEqualTo(200);
        Long resourceId = uploadResponse.jsonPath().getLong("id");
        assertThat(resourceId).isGreaterThan(0L);
        context.setResourceId(resourceId);

        await()
                .atMost(processingTimeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(processingPollIntervalSeconds, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    Response songResponse = given()
                            .accept(ContentType.JSON)
                            .get("/songs/" + resourceId);
                    assertThat(songResponse.statusCode()).isEqualTo(200);
                });
        context.setSongId(resourceId);
    }
}
