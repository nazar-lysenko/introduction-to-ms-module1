package com.apigateway.config;

import com.apigateway.error.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.Instant;

@Configuration
@RequiredArgsConstructor
public class GatewayConfig {

    private final GatewayProperties properties;

    @Bean
    public RouterFunction<ServerResponse> resourceServiceRoute() {
        return GatewayRouterFunctions.route("resource-service")
                .route(RequestPredicates.path("/resources/**"), HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb("resource-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> songServiceRoute() {
        return GatewayRouterFunctions.route("song-service")
                .route(RequestPredicates.path("/songs/**"), HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb("song-service"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> storageServiceRoute() {
        return GatewayRouterFunctions.route("storage-service")
                .route(RequestPredicates.path("/storages/**"), HandlerFunctions.http())
                .filter(LoadBalancerFilterFunctions.lb("storage-service"))
                .build();
    }

    @Bean
    @Order
    public RouterFunction<ServerResponse> fallbackRoute() {
        return RouterFunctions.route()
                .route(RequestPredicates.path("/actuator/**").negate(), request -> {
                    String path = request.uri().getPath();
                    ErrorResponse body = new ErrorResponse(
                            Instant.now(),
                            404,
                            "Not Found",
                            properties.getNotFoundMessage() + ": " + path,
                            path);

                    return ServerResponse.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(body);
                })
                .build();
    }
}
