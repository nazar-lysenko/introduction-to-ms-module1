package com.resourceprocessor.client;

import com.resourceprocessor.metadata.ResourceMetadata;
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
public class SongServiceClient {

    private static final String SONGS_PATH = "/songs";

    @Value("${song-service.url}")
    private String songServiceUrl;

    private final RestTemplate restTemplate;

    @Retryable(
            retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttemptsExpression = "${retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${retry.initial-delay:1000}",
                    multiplierExpression = "${retry.multiplier:2}"
            )
    )
    public void createSong(ResourceMetadata metadata) {
        log.info("Creating song metadata for resourceId={}", metadata.id());
        restTemplate.postForObject(songServiceUrl + SONGS_PATH, metadata, Void.class);
    }
}
