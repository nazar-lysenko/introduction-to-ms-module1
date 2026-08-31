package com.e2e.auth;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * Applies the identity currently selected in {@link AuthContext} to every outgoing request,
 * so individual step definitions do not have to deal with tokens.
 */
public class BearerTokenFilter implements Filter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthContext authContext;

    public BearerTokenFilter(AuthContext authContext) {
        this.authContext = authContext;
    }

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        requestSpec.removeHeader(AUTHORIZATION_HEADER);

        String token = authContext.currentToken();
        if (token != null) {
            requestSpec.header(AUTHORIZATION_HEADER, BEARER_PREFIX + token);
        }
        return ctx.next(requestSpec, responseSpec);
    }
}
