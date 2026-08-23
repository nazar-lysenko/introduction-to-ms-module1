package com.resourceprocessor.messaging;

import com.resourceprocessor.client.ResourceServiceClient;
import com.resourceprocessor.client.SongServiceClient;
import com.resourceprocessor.metadata.ResourceMetadata;
import com.resourceprocessor.metadata.ResourceMetadataExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceEventListener {
    private final ResourceServiceClient resourceServiceClient;
    private final ResourceMetadataExtractorService metadataExtractorService;
    private final SongServiceClient songServiceClient;
    private final ResourceProcessedEventPublisher resourceProcessedEventPublisher;

    @KafkaListener(topics = "${kafka.topic.resource-events}")
    public void onResourceCreated(Long resourceId) {
        log.info("Received resource created event for resourceId={}", resourceId);
        byte[] resourceData = resourceServiceClient.getResource(resourceId);
        ResourceMetadata metadata = metadataExtractorService.extract(resourceId, resourceData);
        songServiceClient.createSong(metadata);
        resourceProcessedEventPublisher.publishResourceProcessed(resourceId);
        log.info("Successfully processed resource id={}", resourceId);
    }
}
