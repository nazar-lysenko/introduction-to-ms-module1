package com.resourceprocessor.messaging;

import com.resourceprocessor.client.ResourceServiceClient;
import com.resourceprocessor.client.SongServiceClient;
import com.resourceprocessor.metadata.MetadataExtractionException;
import com.resourceprocessor.metadata.ResourceMetadata;
import com.resourceprocessor.metadata.ResourceMetadataExtractorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceEventListenerTest {
    private static final byte[] TEST_DATA = new byte[]{};
    private static final Long TEST_RESOURCE_ID = 1L;
    private static final String TEST_SONG_TITLE = "Song";
    private static final String TEST_ARTIST = "Artist";
    private static final String TEST_ALBUM = "Album";
    private static final String TEST_DURATION = "03:30";
    private static final String TEST_YEAR = "2026";

    @Mock
    private ResourceServiceClient resourceServiceClient;

    @Mock
    private ResourceMetadataExtractorService metadataExtractorService;

    @Mock
    private SongServiceClient songServiceClient;

    @Mock
    private ResourceProcessedEventPublisher resourceProcessedEventPublisher;

    @InjectMocks
    private ResourceEventListener resourceEventListener;

    @Test
    void shouldDownloadExtractAndPostMetadata() {
        ResourceMetadata metadata = new ResourceMetadata(
                TEST_RESOURCE_ID,
                TEST_SONG_TITLE,
                TEST_ARTIST,
                TEST_ALBUM,
                TEST_DURATION,
                TEST_YEAR
        );

        when(resourceServiceClient.getResource(TEST_RESOURCE_ID)).thenReturn(TEST_DATA);
        when(metadataExtractorService.extract(TEST_RESOURCE_ID, TEST_DATA)).thenReturn(metadata);

        resourceEventListener.onResourceCreated(TEST_RESOURCE_ID);

        verify(resourceServiceClient, Mockito.times(1)).getResource(TEST_RESOURCE_ID);
        verify(metadataExtractorService, Mockito.times(1)).extract(TEST_RESOURCE_ID, TEST_DATA);
        verify(songServiceClient, Mockito.times(1)).createSong(metadata);
        verify(resourceProcessedEventPublisher, Mockito.times(1)).publishResourceProcessed(TEST_RESOURCE_ID);
    }

    @Test
    void shouldNotExtractMetadataWhenResourceDownloadFails() {
        when(resourceServiceClient.getResource(TEST_RESOURCE_ID)).thenThrow(new RuntimeException("Connection refused"));

        assertThatThrownBy(() -> resourceEventListener.onResourceCreated(TEST_RESOURCE_ID)).isInstanceOf(RuntimeException.class);

        verify(metadataExtractorService, never()).extract(any(), any());
        verify(songServiceClient, never()).createSong(any());
    }

    @Test
    void shouldNotPostMetadataWhenExtractionFails() {
        when(resourceServiceClient.getResource(TEST_RESOURCE_ID)).thenReturn(TEST_DATA);
        when(metadataExtractorService.extract(TEST_RESOURCE_ID, TEST_DATA))
                .thenThrow(new MetadataExtractionException("Extraction failed", new RuntimeException()));

        assertThatThrownBy(() -> resourceEventListener.onResourceCreated(TEST_RESOURCE_ID))
                .isInstanceOf(MetadataExtractionException.class);

        verify(songServiceClient, never()).createSong(any());
    }
}
