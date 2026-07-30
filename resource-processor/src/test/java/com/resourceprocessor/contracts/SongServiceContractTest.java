package com.resourceprocessor.contracts;

import com.resourceprocessor.client.SongServiceClient;
import com.resourceprocessor.metadata.ResourceMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThatNoException;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureStubRunner(
        ids = "com.songservice:song-service:+:stubs:8081",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class SongServiceContractTest {
    private static final long TEST_RESOURCE_ID = 1L;
    private static final String TEST_SONG_TITLE = "Test Song";
    private static final String TEST_ARTIST = "Test Artist";
    private static final String TEST_ALBUM = "Test Album";
    private static final String TEST_DURATION = "06:22";
    private static final String TEST_YEAR = "2020";

    @Autowired
    private SongServiceClient songServiceClient;

    @Test
    void shouldCreateSongMetadataSuccessfully() {
        ResourceMetadata metadata = new ResourceMetadata(
                TEST_RESOURCE_ID, TEST_SONG_TITLE, TEST_ARTIST, TEST_ALBUM, TEST_DURATION, TEST_YEAR
        );

        assertThatNoException().isThrownBy(() -> songServiceClient.createSong(metadata));
    }
}
