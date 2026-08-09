package com.orderplatform.notification.service;

import com.orderplatform.notification.dto.NotificationResponse;
import com.orderplatform.notification.entity.Notification;
import com.orderplatform.notification.entity.NotificationStatus;
import com.orderplatform.notification.exception.ResourceNotFoundException;
import com.orderplatform.notification.mapper.NotificationMapper;
import com.orderplatform.notification.repository.NotificationRepository;
import com.orderplatform.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void getNotificationsForUser_returnsMappedList() {
        Notification notification = Notification.builder()
                .id(1L).userId(5L).orderId(1L).message("Your order #1 has been placed successfully.")
                .status(NotificationStatus.SENT).createdAt(LocalDateTime.now())
                .build();
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(notification));
        when(notificationMapper.toResponse(notification)).thenReturn(
                NotificationResponse.builder().id(1L).userId(5L).orderId(1L).status(NotificationStatus.SENT).build());

        List<NotificationResponse> responses = notificationService.getNotificationsForUser(5L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUserId()).isEqualTo(5L);
    }

    @Test
    void getNotificationById_throwsResourceNotFoundException_whenMissingOrNotOwnedByUser() {
        when(notificationRepository.findByIdAndUserId(99L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getNotificationById(99L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
