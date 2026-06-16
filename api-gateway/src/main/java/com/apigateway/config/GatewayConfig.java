package com.apigateway.config;

import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayConfig {

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
}
