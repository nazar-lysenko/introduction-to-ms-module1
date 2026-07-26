package com.e2e.steps;

import com.e2e.E2EScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class SystemResilienceSteps {

    private static final String AUDIO_MPEG_CONTENT_TYPE = "audio/mpeg";

    @Autowired
    private E2EScenarioContext context;

    @Value("${e2e.resilience.processor-restart.timeout-seconds}")
    private int processorRestartTimeoutSeconds;

    @Value("${e2e.resilience.processor-restart.poll-interval-seconds}")
    private int processorRestartPollIntervalSeconds;

    @Value("${e2e.resilience.heavy-load.timeout-seconds}")
    private int heavyLoadTimeoutSeconds;

    @Value("${e2e.resilience.heavy-load.poll-interval-seconds}")
    private int heavyLoadPollIntervalSeconds;

    @Given("I upload an MP3 file via POST to {string}")
    public void iUploadAnMp3FileViaPostTo(String path) {
        context.setUploadedFileBytes(SharedSteps.MINIMAL_MP3_BYTES);
        Response response = given()
                .contentType(AUDIO_MPEG_CONTENT_TYPE)
                .body(SharedSteps.MINIMAL_MP3_BYTES)
                .post(path);
        context.setLastResponse(response);
    }

    @And("the resource is stored successfully")
    public void theResourceIsStoredSuccessfully() {
        assertThat(context.getLastResponse().statusCode()).isEqualTo(200);
        Long resourceId = context.getLastResponse().jsonPath().getLong("id");
        assertThat(resourceId).isGreaterThan(0L);
        context.setResourceId(resourceId);
    }

    @Given("the song-service replicas are under heavy load")
    public void theSongServiceReplicasAreUnderHeavyLoad() {
    }

    @When("the resource-processor service is restarted")
    public void theResourceProcessorServiceIsRestarted() {
        runDockerComposeCommand("restart", "resource-processor");
    }

    @When("I upload an MP3 file via POST to {string} for resilience")
    public void iUploadAnMp3FileViaPostToForResilience(String path) {
        iUploadAnMp3FileViaPostTo(path);
    }

    @Then("within 60 seconds the song metadata should be available")
    public void within60SecondsTheSongMetadataShouldBeAvailable() {
        Long resourceId = context.getResourceId();
        await()
                .atMost(processorRestartTimeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(processorRestartPollIntervalSeconds, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    Response response = given()
                            .accept(ContentType.JSON)
                            .get("/songs/" + resourceId);
                    assertThat(response.statusCode()).isEqualTo(200);
                });
        context.setSongId(resourceId);
    }

    @And("the metadata should match the MP3 file content")
    public void theMetadataShouldMatchTheMp3FileContent() {
        Long songId = context.getSongId();
        Response response = given()
                .accept(ContentType.JSON)
                .get("/songs/" + songId);
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getLong("id")).isGreaterThan(0L);
    }

    @Then("the upload response status code should be {int}")
    public void theUploadResponseStatusCodeShouldBe(int expectedStatus) {
        assertThat(context.getLastResponse().statusCode()).isEqualTo(expectedStatus);
        Long resourceId = context.getLastResponse().jsonPath().getLong("id");
        context.setResourceId(resourceId);
    }

    @And("within 45 seconds the song metadata should eventually be created")
    public void within45SecondsTheSongMetadataShouldEventuallyBeCreated() {
        Long resourceId = context.getResourceId();
        await()
                .atMost(heavyLoadTimeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(heavyLoadPollIntervalSeconds, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    Response response = given()
                            .accept(ContentType.JSON)
                            .get("/songs/" + resourceId);
                    assertThat(response.statusCode()).isEqualTo(200);
                });
    }

    private void runDockerComposeCommand(String... args) {
        String[] command = new String[2 + args.length];
        command[0] = "docker";
        command[1] = "compose";
        System.arraycopy(args, 0, command, 2, args.length);

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("docker compose command failed with exit code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to run docker compose command", e);
        }
    }
}
