package com.orderplatform.order.kafka;

import com.orderplatform.order.kafka.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Value("${kafka.topic.order-events}")
    private String orderEventsTopic;

    public void publishOrderCreated(OrderCreatedEvent event) {
        String key = String.valueOf(event.getUserId());

        kafkaTemplate.send(orderEventsTopic, key, event).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OrderCreatedEvent for orderId={}", event.getOrderId(), ex);
            } else {
                log.info("Published OrderCreatedEvent for orderId={} to partition={}",
                        event.getOrderId(), result.getRecordMetadata().partition());
            }
        });
    }
}
