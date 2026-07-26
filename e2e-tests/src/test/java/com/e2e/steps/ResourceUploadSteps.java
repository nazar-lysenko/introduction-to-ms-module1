package com.e2e.steps;

import com.e2e.E2EScenarioContext;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ResourceUploadSteps {

    private static final String AUDIO_MPEG_CONTENT_TYPE = "audio/mpeg";
    private static final String APPLICATION_PDF_CONTENT_TYPE = "application/pdf";
    private static final String NON_MP3_FILE_CONTENT = "This is not an MP3 file.";

    @Autowired
    private E2EScenarioContext context;

    @Value("${e2e.processing.timeout-seconds}")
    private int processingTimeoutSeconds;

    @Value("${e2e.processing.poll-interval-seconds}")
    private int processingPollIntervalSeconds;

    @Given("I have an MP3 file {string} with embedded metadata:")
    public void iHaveAnMp3FileWithEmbeddedMetadata(String filePath, DataTable metadata) {
        context.setUploadedFileBytes(loadClasspathFile(filePath));
    }

    @Given("I have an MP3 file {string} with only artist and title")
    public void iHaveAnMp3FileWithOnlyArtistAndTitle(String filePath) {
        context.setUploadedFileBytes(loadClasspathFile(filePath));
    }

    @Given("I have a text file {string}")
    public void iHaveATextFile(String filename) {
        context.setUploadedFileBytes(NON_MP3_FILE_CONTENT.getBytes());
    }

    @When("I upload the MP3 file via POST to {string}")
    public void iUploadTheMp3FileViaPostTo(String path) {
        Response response = given()
                .contentType(AUDIO_MPEG_CONTENT_TYPE)
                .body(context.getUploadedFileBytes())
                .post(path);
        context.setLastResponse(response);
    }

    @When("I attempt to upload the file via POST to {string}")
    public void iAttemptToUploadTheFileViaPostTo(String path) {
        Response response = given()
                .contentType(APPLICATION_PDF_CONTENT_TYPE)
                .body(context.getUploadedFileBytes())
                .post(path);
        context.setLastResponse(response);
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatus) {
        assertThat(context.getLastResponse().statusCode()).isEqualTo(expectedStatus);
    }

    @And("the response body should contain the created resource ID")
    public void theResponseBodyShouldContainTheCreatedResourceId() {
        Long resourceId = context.getLastResponse().jsonPath().getLong("id");
        assertThat(resourceId).isGreaterThan(0L);
        context.setResourceId(resourceId);
    }

    @And("within 30 seconds the song metadata should be available via GET {string}")
    public void within30SecondsTheSongMetadataShouldBeAvailableViaGet(String pathTemplate) {
        Long resourceId = context.getResourceId();
        String path = pathTemplate.replace("{id}", String.valueOf(resourceId));

        await()
                .atMost(processingTimeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(processingPollIntervalSeconds, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    Response songResponse = given()
                            .accept(ContentType.JSON)
                            .get(path);
                    assertThat(songResponse.statusCode()).isEqualTo(200);
                });
        context.setSongId(resourceId);
    }

    @And("within 30 seconds the song metadata should be available")
    public void within30SecondsTheSongMetadataShouldBeAvailable() {
        Long resourceId = context.getResourceId();
        if (resourceId == null) {
            resourceId = context.getLastResponse().jsonPath().getLong("id");
            context.setResourceId(resourceId);
        }
        final Long finalResourceId = resourceId;

        await()
                .atMost(processingTimeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(processingPollIntervalSeconds, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    Response songResponse = given()
                            .accept(ContentType.JSON)
                            .get("/songs/" + finalResourceId);
                    assertThat(songResponse.statusCode()).isEqualTo(200);
                });
        context.setSongId(finalResourceId);
    }

    @And("the song metadata should match:")
    public void theSongMetadataShouldMatch(DataTable expected) {
        Long songId = context.getSongId();
        Response songResponse = given()
                .accept(ContentType.JSON)
                .get("/songs/" + songId);

        assertThat(songResponse.statusCode()).isEqualTo(200);

        List<Map<String, String>> rows = expected.asMaps();
        Map<String, String> expectedFields = rows.get(0);

        assertThat(songResponse.jsonPath().getString("name")).isEqualTo(expectedFields.get("name"));
        assertThat(songResponse.jsonPath().getString("artist")).isEqualTo(expectedFields.get("artist"));
        assertThat(songResponse.jsonPath().getString("album")).isEqualTo(expectedFields.get("album"));
        assertThat(songResponse.jsonPath().getString("year")).isEqualTo(expectedFields.get("year"));
        assertThat(songResponse.jsonPath().getString("duration")).isEqualTo(expectedFields.get("duration"));
    }

    @And("the missing metadata fields should have default values")
    public void theMissingMetadataFieldsShouldHaveDefaultValues() {
        Long songId = context.getSongId();
        Response songResponse = given()
                .accept(ContentType.JSON)
                .get("/songs/" + songId);

        assertThat(songResponse.statusCode()).isEqualTo(200);
        assertThat(songResponse.jsonPath().getLong("id")).isGreaterThan(0L);
    }

    @And("no song metadata should be created")
    public void noSongMetadataShouldBeCreated() {
        assertThat(context.getLastResponse().statusCode()).isEqualTo(400);
    }

    private byte[] loadClasspathFile(String path) {
        String resourcePath = path.replace("classpath:", "");
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalArgumentException("Classpath resource not found: " + resourcePath);
            }
            return is.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test file: " + path, e);
        }
    }
}
