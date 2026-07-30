package com.e2e.steps;

import com.e2e.E2EScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class ResourceRetrievalSteps {

    @Autowired
    private E2EScenarioContext context;

    @When("I send a GET request to {string}")
    public void iSendAGetRequestTo(String pathTemplate) {
        String path = resolvePath(pathTemplate);
        Response response = given()
                .accept("audio/mpeg, application/json, */*")
                .get(path);
        context.setLastResponse(response);
    }

    @And("the response content type should be {string}")
    public void theResponseContentTypeShouldBe(String expectedContentType) {
        String actual = context.getLastResponse().getContentType();
        assertThat(actual).containsIgnoringCase(expectedContentType);
    }

    @And("the response body should be identical to the originally uploaded MP3 file")
    public void theResponseBodyShouldBeIdenticalToTheOriginallyUploadedMp3File() {
        byte[] responseBody = context.getLastResponse().asByteArray();
        assertThat(responseBody).isNotEmpty();
        assertThat(responseBody).isEqualTo(context.getUploadedFileBytes());
    }

    @And("the response should contain valid song metadata fields")
    public void theResponseShouldContainValidSongMetadataFields() {
        Response response = context.getLastResponse();
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getLong("id")).isGreaterThan(0L);
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
}
