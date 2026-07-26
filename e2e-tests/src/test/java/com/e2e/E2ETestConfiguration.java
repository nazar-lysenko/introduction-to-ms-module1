package com.e2e;

import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

@CucumberContextConfiguration
@SpringBootTest(classes = E2ETestApplication.class)
@Tag("e2e")
public class E2ETestConfiguration {

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Value("${e2e.health-check.path}")
    private String healthCheckPath;

    @Value("${e2e.health-check.timeout-seconds}")
    private int healthCheckTimeoutSeconds;

    @Value("${e2e.health-check.poll-interval-seconds}")
    private int healthCheckPollIntervalSeconds;

    @PostConstruct
    public void configureRestAssured() {
        RestAssured.baseURI = gatewayUrl;
        waitForSystemReady();
    }

    private void waitForSystemReady() {
        await()
                .atMost(healthCheckTimeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(healthCheckPollIntervalSeconds, TimeUnit.SECONDS)
                .ignoreExceptions()
                .untilAsserted(() ->
                        given()
                                .baseUri(gatewayUrl)
                                .get(healthCheckPath)
                                .then()
                                .statusCode(404)
                );
    }
}
