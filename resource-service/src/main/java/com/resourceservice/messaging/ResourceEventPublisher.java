package com.resourceservice.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceEventPublisher {

    @Value("${kafka.topic.resource-events}")
    private String resourceEventsTopic;

    private final KafkaTemplate<String, Long> kafkaTemplate;

    public void publishResourceCreated(Long resourceId) {
        log.info("Publishing resource created event for resourceId={}", resourceId);
        kafkaTemplate.send(resourceEventsTopic, resourceId);
    }
}
