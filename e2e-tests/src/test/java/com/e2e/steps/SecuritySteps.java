package com.e2e.steps;

import com.e2e.E2EScenarioContext;
import com.e2e.auth.AuthContext;
import io.cucumber.java.After;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class SecuritySteps {

    private static final String STORAGES_PATH = "/storages";
    private static final String AUDIO_MPEG_CONTENT_TYPE = "audio/mpeg";
    private static final Map<String, Object> STORAGE_PAYLOAD = Map.of(
            "storageType", "STAGING",
            "bucket", "e2e-security-bucket",
            "path", "/e2e-security");

    @Autowired
    private E2EScenarioContext context;

    @Autowired
    private AuthContext authContext;

    @Value("${e2e.storage-service.url}")
    private String storageServiceUrl;

    private Long createdStorageId;

    @Given("I am not authenticated")
    public void iAmNotAuthenticated() {
        authContext.useNoToken();
    }

    @Given("I present a forged JWT token")
    public void iPresentAForgedJwtToken() {
        authContext.useForgedToken();
    }

    @Given("I am authenticated as an admin")
    public void iAmAuthenticatedAsAnAdmin() {
        authContext.useAdmin();
    }

    @Given("I am authenticated as a regular user")
    public void iAmAuthenticatedAsARegularUser() {
        authContext.useRegularUser();
    }

    @When("I send a {string} request to {string}")
    public void iSendARequestTo(String method, String pathTemplate) {
        context.setLastResponse(send(given().accept(ContentType.JSON), method, resolvePath(pathTemplate)));
    }

    @When("I send a {string} request directly to the storage service at {string}")
    public void iSendARequestDirectlyToTheStorageService(String method, String pathTemplate) {
        RequestSpecification spec = given().baseUri(storageServiceUrl).accept(ContentType.JSON);
        context.setLastResponse(send(spec, method, resolvePath(pathTemplate)));
    }

    @When("I attempt to create a storage entry")
    public void iAttemptToCreateAStorageEntry() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body(STORAGE_PAYLOAD)
                .post(STORAGES_PATH);
        context.setLastResponse(response);

        if (response.statusCode() == 200) {
            createdStorageId = response.jsonPath().getLong("id");
        }
    }

    @When("I attempt to upload an MP3 file to {string}")
    public void iAttemptToUploadAnMp3FileTo(String path) {
        context.setLastResponse(given()
                .contentType(AUDIO_MPEG_CONTENT_TYPE)
                .body(SharedSteps.MINIMAL_MP3_BYTES)
                .post(path));
    }

    @And("I delete the storage entry created in this scenario")
    public void iDeleteTheStorageEntryCreatedInThisScenario() {
        assertThat(createdStorageId)
                .as("no storage entry was created earlier in this scenario")
                .isNotNull();

        Response response = given()
                .accept(ContentType.JSON)
                .delete(STORAGES_PATH + "?id=" + createdStorageId);
        context.setLastResponse(response);

        if (response.statusCode() == 200) {
            createdStorageId = null;
        }
    }

    @And("the seeded storage entries should still be present")
    public void theSeededStorageEntriesShouldStillBePresent() {
        authContext.useAdmin();
        Response response = given().accept(ContentType.JSON).get(STORAGES_PATH);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("storageType", String.class))
                .contains("STAGING", "PERMANENT");
    }

    @After
    public void removeLeftoverStorageEntry() {
        if (createdStorageId == null) {
            return;
        }
        authContext.useAdmin();
        given().delete(STORAGES_PATH + "?id=" + createdStorageId);
        createdStorageId = null;
    }

    private Response send(RequestSpecification spec, String method, String path) {
        return switch (method.toUpperCase()) {
            case "GET" -> spec.get(path);
            case "POST" -> spec.contentType(ContentType.JSON).body(STORAGE_PAYLOAD).post(path);
            case "DELETE" -> spec.delete(path);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };
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
