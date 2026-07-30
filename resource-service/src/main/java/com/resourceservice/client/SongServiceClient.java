package com.resourceservice.client;

import com.resourceservice.config.SongServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongServiceClient {
    private static final String SONG_SERVICE_ROOT_PATH = "/songs";
    private static final String SONG_SERVICE_ID_PARAM = "id";

    private final RestTemplate restTemplate;
    private final SongServiceProperties songServiceProperties;

    @Retryable(
            retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttemptsExpression = "${retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${retry.initial-delay:1000}",
                    multiplierExpression = "${retry.multiplier:2}"
            )
    )
    public void deleteSongMetadata(List<Long> ids) {
        String csvIds = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        URI uri = UriComponentsBuilder.fromUriString(songServiceProperties.getUrl())
                .path(SONG_SERVICE_ROOT_PATH)
                .queryParam(SONG_SERVICE_ID_PARAM, csvIds)
                .build()
                .toUri();

        restTemplate.delete(uri);
    }
}
