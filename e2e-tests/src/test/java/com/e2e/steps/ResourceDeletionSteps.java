package com.e2e.steps;

import com.e2e.E2EScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ResourceDeletionSteps {

    @Autowired
    private E2EScenarioContext context;

    @Autowired
    private SharedSteps sharedSteps;

    @Value("${e2e.deletion.timeout-seconds}")
    private int deletionTimeoutSeconds;

    @Value("${e2e.deletion.poll-interval-seconds}")
    private int deletionPollIntervalSeconds;

    @And("I can confirm the song metadata exists via GET {string}")
    public void iCanConfirmTheSongMetadataExistsViaGet(String pathTemplate) {
        String path = resolvePath(pathTemplate);
        Response response = given()
                .accept(ContentType.JSON)
                .get(path);
        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Given("resources have been uploaded with IDs stored as {string} and {string}")
    public void resourcesHaveBeenUploadedWithIdsStoredAsAnd(String name1, String name2) {
        sharedSteps.uploadAndWaitForProcessing();
        context.putNamedResourceId(name1, context.getResourceId());
        context.setResourceId(null);
        context.setSongId(null);

        sharedSteps.uploadAndWaitForProcessing();
        context.putNamedResourceId(name2, context.getResourceId());
        context.setResourceId(null);
        context.setSongId(null);
    }

    @When("I send a DELETE request to {string}")
    public void iSendADeleteRequestTo(String pathTemplate) {
        String path = resolveDeletePath(pathTemplate);
        Response response = given()
                .accept(ContentType.JSON)
                .delete(path);
        context.setLastResponse(response);
    }

    @And("the resource should no longer be retrievable via GET {string}")
    public void theResourceShouldNoLongerBeRetrievableViaGet(String pathTemplate) {
        String path = resolvePath(pathTemplate);
        Response response = given().get(path);
        assertThat(response.statusCode()).isEqualTo(404);
    }

    @And("the associated song metadata should no longer exist via GET {string}")
    public void theAssociatedSongMetadataShouldNoLongerExistViaGet(String pathTemplate) {
        String path = resolvePath(pathTemplate);
        await()
                .atMost(deletionTimeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(deletionPollIntervalSeconds, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() -> {
                    Response response = given().get(path);
                    assertThat(response.statusCode()).isEqualTo(404);
                });
    }

    @And("neither resource should be retrievable")
    public void neitherResourceShouldBeRetrievable() {
        for (Map.Entry<String, Long> entry : context.getNamedResourceIds().entrySet()) {
            Response response = given().get("/resources/" + entry.getValue());
            assertThat(response.statusCode())
                    .as("Resource '%s' (id=%d) should return 404", entry.getKey(), entry.getValue())
                    .isEqualTo(404);
        }
    }

    @And("neither song metadata record should exist")
    public void neitherSongMetadataRecordShouldExist() {
        for (Map.Entry<String, Long> entry : context.getNamedResourceIds().entrySet()) {
            final Long resourceId = entry.getValue();
            await()
                    .atMost(deletionTimeoutSeconds, TimeUnit.SECONDS)
                    .pollInterval(deletionPollIntervalSeconds, TimeUnit.SECONDS)
                    .ignoreExceptions()
                    .untilAsserted(() -> {
                        Response response = given().get("/songs/" + resourceId);
                        assertThat(response.statusCode())
                                .as("Song for resource '%s' (id=%d) should return 404", entry.getKey(), resourceId)
                                .isEqualTo(404);
                    });
        }
    }

    private String resolvePath(String pathTemplate) {
        String path = pathTemplate;
        if (context.getResourceId() != null) {
            path = path.replace("{resourceId}", String.valueOf(context.getResourceId()));
        }
        if (context.getSongId() != null) {
            path = path.replace("{songId}", String.valueOf(context.getSongId()));
        }
        return path;
    }

    private String resolveDeletePath(String pathTemplate) {
        String path = resolvePath(pathTemplate);
        for (Map.Entry<String, Long> entry : context.getNamedResourceIds().entrySet()) {
            path = path.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return path;
    }
}
