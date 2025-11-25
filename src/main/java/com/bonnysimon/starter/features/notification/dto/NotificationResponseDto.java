package com.bonnysimon.starter.features.notification.dto;

import com.bonnysimon.starter.features.notification.NotificationEntity;
import lombok.Data;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
public class NotificationResponseDto {

    private Long id;
    private String title;
    private String description;
    private String forName;
    private Long forId;
    private boolean isRead;
    private String expiresAt;
    private Long userId;
    private Long notifiedPersonnelId;
    private String redirectUrl;
    private String group;
    private String createdAt;
    private String formattedCreatedAt;
    private String updatedAt;
    private String userName;
    private String notifiedPersonnelName;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static NotificationResponseDto fromEntity(NotificationEntity notification) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setDescription(notification.getDescription());
        dto.setForName(notification.getForName());
        dto.setForId(notification.getForId());
        dto.setExpiresAt(notification.getExpiresAt());
        dto.setRedirectUrl(notification.getRedirectUrl());
        dto.setGroup(notification.getGroupName());

        dto.setUpdatedAt(notification.getUpdatedAt() != null ? notification.getUpdatedAt().toString() : null);
        dto.setCreatedAt(notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : null);
//        dto.setFormattedCreatedAt(notification.getCreatedAt() != null ? FORMATTER.format(notification.getCreatedAt()) : null);

        if (notification.getUser() != null) {
            dto.setUserId(notification.getUser().getId());
            dto.setUserName(notification.getUser().getName());
        }

        if (notification.getNotifiedPersonnel() != null) {
            dto.setNotifiedPersonnelId(notification.getNotifiedPersonnel().getId());
            dto.setNotifiedPersonnelName(notification.getNotifiedPersonnel().getName());
        }

        return dto;
    }
}
