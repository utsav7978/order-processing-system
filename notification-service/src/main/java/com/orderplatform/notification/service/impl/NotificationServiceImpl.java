package com.orderplatform.notification.service.impl;

import com.orderplatform.notification.cache.RedisConfig;
import com.orderplatform.notification.dto.NotificationResponse;
import com.orderplatform.notification.exception.ResourceNotFoundException;
import com.orderplatform.notification.mapper.NotificationMapper;
import com.orderplatform.notification.repository.NotificationRepository;
import com.orderplatform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Cacheable(value = RedisConfig.NOTIFICATION_CACHE, key = "#userId")
    public List<NotificationResponse> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public NotificationResponse getNotificationById(Long id, Long userId) {
        return notificationRepository.findByIdAndUserId(id, userId)
                .map(notificationMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No notification found with id: " + id));
    }
}
