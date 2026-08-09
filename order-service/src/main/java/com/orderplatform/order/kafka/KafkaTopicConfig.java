package com.orderplatform.order.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.order-events}")
    private String orderEventsTopic;

    @Bean
    public NewTopic orderEventsTopic() {
        // 3 partitions to demonstrate partition-key usage (keyed by userId
        // in OrderEventProducer, so one user's events stay ordered).
        return TopicBuilder.name(orderEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
