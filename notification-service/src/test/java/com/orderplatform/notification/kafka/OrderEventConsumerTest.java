package com.orderplatform.notification.kafka;

import com.orderplatform.notification.cache.RedisConfig;
import com.orderplatform.notification.entity.Notification;
import com.orderplatform.notification.entity.NotificationStatus;
import com.orderplatform.notification.kafka.event.OrderCreatedEvent;
import com.orderplatform.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @InjectMocks
    private OrderEventConsumer consumer;

    @Test
    void handleOrderCreated_savesSentNotification_andEvictsUserCache() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(1L)
                .userId(5L)
                .totalAmount(new BigDecimal("99.98"))
                .createdAt(java.time.LocalDateTime.now())
                .build();

        when(cacheManager.getCache(RedisConfig.NOTIFICATION_CACHE)).thenReturn(cache);

        consumer.handleOrderCreated(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(5L);
        assertThat(saved.getOrderId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getMessage()).contains("1").contains("99.98");

        verify(cache).evict(5L);
    }

    @Test
    void handleOrderCreated_letsExceptionPropagate_whenSaveThrows() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(2L)
                .userId(7L)
                .totalAmount(new BigDecimal("20.00"))
                .createdAt(java.time.LocalDateTime.now())
                .build();

        when(notificationRepository.save(any(Notification.class)))
                .thenThrow(new RuntimeException("DB down"));

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> consumer.handleOrderCreated(event));

        // Exactly one attempt at this layer - retrying is the container's
        // job (KafkaConsumerConfig), not this method's.
        verify(notificationRepository, times(1)).save(any(Notification.class));
        // Never reached because save() threw before this line.
        verifyNoInteractions(cacheManager);
    }
}
