package com.resourceservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitBreakerConfiguration {

    public static final String STORAGE_SERVICE_CB = "storageService";

    @Value("${circuit-breaker.storage-service.sliding-window-size:5}")
    private int slidingWindowSize;

    @Value("${circuit-breaker.storage-service.failure-rate-threshold:50}")
    private float failureRateThreshold;

    @Value("${circuit-breaker.storage-service.wait-duration-seconds:10}")
    private long waitDurationSeconds;

    @Value("${circuit-breaker.storage-service.permitted-calls-in-half-open-state:2}")
    private int permittedCallsInHalfOpenState;

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> storageServiceCbCustomizer() {
        return factory -> factory.configure(
            builder -> builder.circuitBreakerConfig(
                CircuitBreakerConfig.custom()
                    .slidingWindowSize(slidingWindowSize)
                    .failureRateThreshold(failureRateThreshold)
                    .waitDurationInOpenState(Duration.ofSeconds(waitDurationSeconds))
                    .permittedNumberOfCallsInHalfOpenState(permittedCallsInHalfOpenState)
                    .build()
            ),
            STORAGE_SERVICE_CB
        );
    }
}
