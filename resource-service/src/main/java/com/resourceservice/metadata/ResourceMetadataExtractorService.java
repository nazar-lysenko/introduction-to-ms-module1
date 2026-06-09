package com.resourceservice.metadata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.springframework.stereotype.Service;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourceMetadataExtractorService {
    private static final String METADATA_ALBUM_KEY = "xmpDM:album";
    private static final String METADATA_DURATION_KEY = "xmpDM:duration";
    private static final String METADATA_RELEASE_DATE_KEY = "xmpDM:releaseDate";
    private static final String METADATA_DURATION_FORMAT = "%02d:%02d";

    private final AutoDetectParser autoDetectParser;

    public ResourceMetadata extract(Long id, byte[] data) {
        try (InputStream stream = new ByteArrayInputStream(data)) {
            Metadata metadata = new Metadata();

            autoDetectParser.parse(stream, new DefaultHandler(), metadata, new ParseContext());

            return new ResourceMetadata(
                    id,
                    metadata.get(TikaCoreProperties.TITLE),
                    metadata.get(TikaCoreProperties.CREATOR),
                    metadata.get(METADATA_ALBUM_KEY),
                    formatDuration(metadata.get(METADATA_DURATION_KEY)),
                    metadata.get(METADATA_RELEASE_DATE_KEY)
            );
        } catch (Exception e) {
            throw new MetadataExtractionException("Can not extract metadata from resource", e);
        }
    }

    private String formatDuration(String durationSeconds) {
        double seconds = Double.parseDouble(durationSeconds);
        Duration duration = Duration.ofSeconds((long) seconds);

        return String.format(METADATA_DURATION_FORMAT, duration.toMinutes(), duration.toSeconds());
    }
}
