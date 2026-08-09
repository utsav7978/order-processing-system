package com.orderplatform.notification.kafka;

import com.orderplatform.notification.cache.RedisConfig;
import com.orderplatform.notification.entity.Notification;
import com.orderplatform.notification.entity.NotificationStatus;
import com.orderplatform.notification.kafka.event.OrderCreatedEvent;
import com.orderplatform.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Persistence failures are intentionally NOT caught here - they're left to
 * propagate so the container-level error handler (see KafkaConsumerConfig)
 * can retry a bounded number of times and, if it still fails, route the
 * message to the dead-letter topic instead of this method silently marking
 * it FAILED and moving on (that was Phase 6's simplification; Phase 8
 * replaces it with real retry + DLT handling).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final NotificationRepository notificationRepository;
    private final CacheManager cacheManager;

    @KafkaListener(
            topics = "${kafka.topic.order-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        String message = "Your order #" + event.getOrderId() + " for $" + event.getTotalAmount()
                + " has been placed successfully.";

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .orderId(event.getOrderId())
                .message(message)
                .status(NotificationStatus.SENT)
                .build();

        // "Pretend to send Email" - a real system would call an email provider here.
        log.info("Email sent successfully for order {} to user {}", event.getOrderId(), event.getUserId());

        // Let this throw on failure - do not catch. The container's
        // DefaultErrorHandler (KafkaConsumerConfig) owns retry + DLT.
        notificationRepository.save(notification);

        evictNotificationCache(event.getUserId());
    }

    private void evictNotificationCache(Long userId) {
        var cache = cacheManager.getCache(RedisConfig.NOTIFICATION_CACHE);
        if (cache != null) {
            cache.evict(userId);
        }
    }
}
