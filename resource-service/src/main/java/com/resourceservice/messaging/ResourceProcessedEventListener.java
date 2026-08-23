package com.resourceservice.messaging;

import com.resourceservice.resource.ResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceProcessedEventListener {

    private final ResourceService resourceService;

    @KafkaListener(topics = "${kafka.topic.resource-processed-events}")
    public void onResourceProcessed(Long resourceId) {
        log.info("Received resource processed event for resourceId={}", resourceId);
        resourceService.promoteResourceToPermanent(resourceId);
    }
}
