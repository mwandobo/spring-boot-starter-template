package com.bonnysimon.starter.features.notification.dto;

import com.bonnysimon.starter.features.notification.enums.NotificationChannelsEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationDto {

    @NotNull
    private NotificationChannelsEnum channel;
    @NotBlank(message = "Template is required")
    private String template;
    private Long userId;
    private String subject;
    private String description;
    private String forName;
    private String forId;
    private Boolean isRead;
    private String expiresAt;
    private java.util.List<String> recipients;
    private String redirectUrl;
    private String groupName;
    private Map<String, Object> context;
}
