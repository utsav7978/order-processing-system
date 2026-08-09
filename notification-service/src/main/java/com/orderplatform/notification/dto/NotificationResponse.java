package com.orderplatform.notification.dto;

import com.orderplatform.notification.entity.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse implements Serializable {

    private Long id;
    private Long userId;
    private Long orderId;
    private String message;
    private NotificationStatus status;
    private LocalDateTime createdAt;
}
