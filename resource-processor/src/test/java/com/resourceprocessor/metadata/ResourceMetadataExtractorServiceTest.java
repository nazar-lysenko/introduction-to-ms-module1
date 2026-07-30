package com.resourceprocessor.metadata;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ResourceMetadataExtractorServiceTest {
    private static final long TEST_RESOURCE_ID = 1L;
    private static final byte[] TEST_DATA = new byte[]{};

    private static final int METADATA_ARGUMENT_INDEX = 2;

    private static final String TEST_SONG_TITLE = "Song Title";
    private static final String TEST_CREATOR = "The Creator";
    private static final String TEST_ALBUM = "The Album";
    private static final String TEST_RELEASE_DATE = "2026";

    private static final String TEST_DEFAULT_DURATION = "65.0";
    private static final String TEST_DEFAULT_FORMATED_DURATION = "01:05";
    private static final String TEST_DECIMAL_DURATION = "245.7";
    private static final String TEST_DECIMAL_FORMATED_DURATION = "04:05";
    private static final String TEST_NAN_DURATION = "not-a-number";

    @Mock
    private AutoDetectParser autoDetectParser;

    @InjectMocks
    private ResourceMetadataExtractorService extractorService;

    @Test
    void shouldExtractAllMetadataFieldsFromValidMp3() throws TikaException, IOException, SAXException {
        Mockito.doAnswer(invocation -> {
            Metadata metadata = invocation.getArgument(METADATA_ARGUMENT_INDEX);

            metadata.set(TikaCoreProperties.TITLE, TEST_SONG_TITLE);
            metadata.set(TikaCoreProperties.CREATOR, TEST_CREATOR);
            metadata.set(MetadataConstants.ALBUM_KEY, TEST_ALBUM);
            metadata.set(MetadataConstants.DURATION_KEY, TEST_DEFAULT_DURATION);
            metadata.set(MetadataConstants.RELEASE_DATE_KEY, TEST_RELEASE_DATE);

            return null;
        }).when(autoDetectParser).parse(any(InputStream.class), any(ContentHandler.class), any(Metadata.class), any(ParseContext.class));

        ResourceMetadata result = extractorService.extract(TEST_RESOURCE_ID, TEST_DATA);

        assertThat(result.id()).isEqualTo(TEST_RESOURCE_ID);
        assertThat(result.name()).isEqualTo(TEST_SONG_TITLE);
        assertThat(result.album()).isEqualTo(TEST_ALBUM);
        assertThat(result.artist()).isEqualTo(TEST_CREATOR);
        assertThat(result.year()).isEqualTo(TEST_RELEASE_DATE);
        assertThat(result.duration()).isEqualTo(TEST_DEFAULT_FORMATED_DURATION);
    }

    @Test
    void shouldReturnNullFieldsWhenMetadataTagsAreMissing() {
        ResourceMetadata result = extractorService.extract(TEST_RESOURCE_ID, TEST_DATA);

        assertThat(result.id()).isEqualTo(TEST_RESOURCE_ID);
        assertThat(result.name()).isNull();
        assertThat(result.artist()).isNull();
        assertThat(result.album()).isNull();
        assertThat(result.duration()).isNull();
        assertThat(result.year()).isNull();
    }

    @Test
    void shouldFormatDurationFromSecondsToMetadataFormat() throws TikaException, IOException, SAXException {
        Mockito.doAnswer(invocation -> {
            Metadata metadata = invocation.getArgument(METADATA_ARGUMENT_INDEX);
            metadata.set(MetadataConstants.DURATION_KEY, TEST_DEFAULT_DURATION);

            return null;
        }).when(autoDetectParser).parse(any(InputStream.class), any(ContentHandler.class), any(Metadata.class), any(ParseContext.class));

        ResourceMetadata result = extractorService.extract(TEST_RESOURCE_ID, TEST_DATA);

        assertThat(result.duration()).isEqualTo(TEST_DEFAULT_FORMATED_DURATION);
    }

    @Test
    void shouldHandleDecimalDurationValue() throws TikaException, IOException, SAXException {
        Mockito.doAnswer(invocation -> {
            Metadata metadata = invocation.getArgument(METADATA_ARGUMENT_INDEX);
            metadata.set(MetadataConstants.DURATION_KEY, TEST_DECIMAL_DURATION);

            return null;
        }).when(autoDetectParser).parse(any(InputStream.class), any(ContentHandler.class), any(Metadata.class), any(ParseContext.class));

        ResourceMetadata result = extractorService.extract(TEST_RESOURCE_ID, TEST_DATA);

        assertThat(result.duration()).isEqualTo(TEST_DECIMAL_FORMATED_DURATION);
    }

    @Test
    void shouldThrowMetadataExtractionExceptionWhenParsesFails() throws TikaException, IOException, SAXException {
        Mockito.doThrow(new RuntimeException("Parses error"))
                .when(autoDetectParser)
                .parse(any(InputStream.class), any(ContentHandler.class), any(Metadata.class), any(ParseContext.class));

        assertThatThrownBy(() -> extractorService.extract(TEST_RESOURCE_ID, TEST_DATA))
                .isInstanceOf(MetadataExtractionException.class)
                .hasMessageContaining(ResourceMetadataExtractorService.METADATA_EXTRACTION_EXCEPTION_MESSAGE)
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldThrowMetadataExtractionExceptionWhenDurationIsNotNumeric() throws TikaException, IOException, SAXException {
        Mockito.doAnswer(invocation -> {
            Metadata metadata = invocation.getArgument(METADATA_ARGUMENT_INDEX);
            metadata.set(MetadataConstants.DURATION_KEY, TEST_NAN_DURATION);

            return null;
        }).when(autoDetectParser).parse(any(InputStream.class), any(ContentHandler.class), any(Metadata.class), any(ParseContext.class));

        assertThatThrownBy(() -> extractorService.extract(TEST_RESOURCE_ID, TEST_DATA))
                .isInstanceOf(MetadataExtractionException.class);
    }
}