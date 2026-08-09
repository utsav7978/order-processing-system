package com.orderplatform.notification.service;

import com.orderplatform.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getNotificationsForUser(Long userId);

    NotificationResponse getNotificationById(Long id, Long userId);
}
