package com.e2e.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
public class KeycloakTokenClient {

    private static final String ACCESS_TOKEN_FIELD = "access_token";
    private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${keycloak.token-url}")
    private String tokenUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    public String passwordGrantToken(String username, String password) {
        String form = "grant_type=password"
                + "&client_id=" + encode(clientId)
                + "&username=" + encode(username)
                + "&password=" + encode(password);

        HttpRequest request = HttpRequest.newBuilder(URI.create(tokenUrl))
                .header("Content-Type", FORM_CONTENT_TYPE)
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Failed to obtain token for '" + username + "': HTTP " + response.statusCode()
                                + " - " + response.body());
            }
            JsonNode body = objectMapper.readTree(response.body());
            return body.path(ACCESS_TOKEN_FIELD).asText();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while requesting a token from Keycloak", e);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to request a token from Keycloak at " + tokenUrl, e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
