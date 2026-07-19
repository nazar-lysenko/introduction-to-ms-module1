package com.resourceservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
public class KafkaProducerConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.resource-events}")
    private String resourceEventsTopic;

    @Value("${kafka.topic.partitions}")
    private int partitions;

    @Value("${kafka.topic.replicas}")
    private int replicas;

    @Bean
    public ProducerFactory<String, Long> producerFactory() {
        return new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LongSerializer.class
        ));
    }

    @Bean
    public KafkaTemplate<String, Long> kafkaTemplate(ProducerFactory<String, Long> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public NewTopic resourceEventsTopic() {
        return TopicBuilder.name(resourceEventsTopic)
                .partitions(partitions)
                .replicas(replicas)
                .build();
    }
}
