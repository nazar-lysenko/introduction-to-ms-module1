package com.resourceprocessor.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceClient {

    private static final String RESOURCE_PATH = "/resources/{id}";

    @Value("${resource-service.url}")
    private String resourceServiceUrl;

    private final RestTemplate restTemplate;

    @Retryable(
            retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttemptsExpression = "${retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${retry.initial-delay:1000}",
                    multiplierExpression = "${retry.multiplier:2}"
            )
    )
    public byte[] getResource(Long id) {
        log.info("Fetching resource data for resourceId={}", id);
        return restTemplate.getForObject(resourceServiceUrl + RESOURCE_PATH, byte[].class, id);
    }
}
