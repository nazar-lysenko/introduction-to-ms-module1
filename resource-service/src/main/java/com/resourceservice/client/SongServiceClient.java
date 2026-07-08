package com.resourceservice.client;

import com.resourceservice.config.SongServiceProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SongServiceClient {
    private static final String SONG_SERVICE_ROOT_PATH = "/songs";
    private static final String SONG_SERVICE_ID_PARAM = "id";

    private final RestTemplate restTemplate;
    private final SongServiceProperties songServiceProperties;

    public void deleteSongMetadata(List<Long> ids) {
        URI uri = UriComponentsBuilder.fromUriString(songServiceProperties.getUrl())
                .path(SONG_SERVICE_ROOT_PATH)
                .queryParam(SONG_SERVICE_ID_PARAM, ids.toArray())
                .build()
                .toUri();

        restTemplate.delete(uri);
    }
}
