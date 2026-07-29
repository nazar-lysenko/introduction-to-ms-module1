package com.apigateway.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

@Component
@RefreshScope
@Getter
public class GatewayProperties {

    @Value("${api.gateway.not-found-message:No route defined for}")
    private String notFoundMessage;
}
