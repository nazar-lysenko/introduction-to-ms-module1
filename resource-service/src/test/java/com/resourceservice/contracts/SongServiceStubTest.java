package com.resourceservice.contracts;

import com.resourceservice.client.SongServiceClient;
import com.resourceservice.config.SongServiceProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;

@SpringBootTest(classes = {
        SongServiceStubTest.TestConfig.class,
        SongServiceClient.class
})
@AutoConfigureStubRunner(
        ids = "com.songservice:song-service:+:stubs:8081",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@TestPropertySource(properties = {
        "song-service.url=http://localhost:8081"
})
class SongServiceStubTest {

    private static final long TEST_SONG_ID_1 = 1L;
    private static final long TEST_SONG_ID_2 = 2L;

    @Autowired
    private SongServiceClient songServiceClient;

    @Test
    void shouldDeleteSongMetadataSuccessfully() {
        assertThatNoException().isThrownBy(() -> songServiceClient.deleteSongMetadata(List.of(TEST_SONG_ID_1, TEST_SONG_ID_2)));
    }

    @Configuration
    @EnableRetry
    @EnableConfigurationProperties(SongServiceProperties.class)
    static class TestConfig {
        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }
    }
}
