package com.e2e.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Holds the credentials that outgoing REST Assured calls are made with.
 * Scenarios switch identity through the {@code Given I am authenticated as ...} steps;
 * every scenario starts as the admin user so the pre-existing journeys keep working.
 */
@Component
public class AuthContext {

    private static final String FORGED_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJoYWNrZXIiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOl"
                    + "siQURNSU4iXX19.not-a-valid-signature";

    private final KeycloakTokenClient tokenClient;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.user.username}")
    private String userUsername;

    @Value("${keycloak.user.password}")
    private String userPassword;

    private String adminToken;
    private String userToken;
    private String currentToken;

    public AuthContext(KeycloakTokenClient tokenClient) {
        this.tokenClient = tokenClient;
    }

    public void useAdmin() {
        currentToken = adminToken();
    }

    public void useRegularUser() {
        currentToken = userToken();
    }

    public void useNoToken() {
        currentToken = null;
    }

    public void useForgedToken() {
        currentToken = FORGED_TOKEN;
    }

    public String currentToken() {
        return currentToken;
    }

    public String adminToken() {
        if (adminToken == null) {
            adminToken = tokenClient.passwordGrantToken(adminUsername, adminPassword);
        }
        return adminToken;
    }

    public String userToken() {
        if (userToken == null) {
            userToken = tokenClient.passwordGrantToken(userUsername, userPassword);
        }
        return userToken;
    }
}
