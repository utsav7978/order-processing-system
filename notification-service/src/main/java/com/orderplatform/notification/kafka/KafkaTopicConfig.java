package com.orderplatform.notification.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.order-events-dlt}")
    private String orderEventsDltTopic;

    @Bean
    public NewTopic orderEventsDltTopic() {
        // Same partition count as order-events, so a poison-pill message's
        // partition maps cleanly onto the DLT.
        return TopicBuilder.name(orderEventsDltTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
