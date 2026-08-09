package com.orderplatform.notification.controller;

import com.orderplatform.notification.dto.NotificationResponse;
import com.orderplatform.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notifications generated from OrderCreatedEvent, cached in Redis")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get my notifications", description = "Cached in Redis under notifications::{userId}")
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(notificationService.getNotificationsForUser(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a notification by id", description = "Scoped to the authenticated user's own notifications")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id,
                                                                       Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(notificationService.getNotificationById(id, userId));
    }

    private Long currentUserId(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}
