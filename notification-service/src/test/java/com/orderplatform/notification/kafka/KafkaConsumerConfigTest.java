package com.orderplatform.notification.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConsumerConfigTest {

    @Test
    void resolveDeadLetterDestination_routesToDltTopic_preservingPartition() {
        KafkaConsumerConfig config = new KafkaConsumerConfig();
        ReflectionTestUtils.setField(config, "dltTopic", "order-events.dlt");

        ConsumerRecord<String, Object> record = new ConsumerRecord<>("order-events", 2, 10L, "5", "payload");

        TopicPartition destination = config.resolveDeadLetterDestination(record, new RuntimeException("boom"));

        assertThat(destination.topic()).isEqualTo("order-events.dlt");
        assertThat(destination.partition()).isEqualTo(2);
    }
}
