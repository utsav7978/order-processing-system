package com.orderplatform.notification.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Resolves the "poison pill" gap called out in Phase 6: without this, a
 * message that keeps throwing (bad data, transient DB outage, etc.) would
 * either be redelivered forever or silently dropped. Now it gets 3 total
 * attempts (1 initial + 2 retries, 1s apart), and if it still fails, it's
 * published to order-events.dlt instead of blocking the partition or
 * looping indefinitely.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${kafka.topic.order-events-dlt}")
    private String dltTopic;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(null));
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate, this::resolveDeadLetterDestination);

        FixedBackOff backOff = new FixedBackOff(1000L, 2);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        // A message that fails to deserialize will never succeed on retry -
        // send it straight to the DLT instead of wasting 2 retries on it.
        errorHandler.addNotRetryableExceptions(DeserializationException.class);

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);
        // Matches order-events' 3 partitions - one consumer thread per partition.
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);

        return factory;
    }

    TopicPartition resolveDeadLetterDestination(ConsumerRecord<?, ?> record, Exception exception) {
        return new TopicPartition(dltTopic, record.partition());
    }
}
