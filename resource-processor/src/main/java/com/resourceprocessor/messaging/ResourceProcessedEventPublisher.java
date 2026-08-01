package com.resourceprocessor.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceProcessedEventPublisher {

    @Value("${kafka.topic.resource-processed-events}")
    private String resourceProcessedTopic;

    private final KafkaTemplate<String, Long> kafkaTemplate;

    public void publishResourceProcessed(Long resourceId) {
        log.info("Publishing resource processed event for resourceId={}", resourceId);
        kafkaTemplate.send(resourceProcessedTopic, resourceId);
    }
}
