package com.bonnysimon.starter.features.notification.dto;

import com.bonnysimon.starter.features.notification.enums.NotificationChannelsEnum;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationDto {

    @NotNull
    private NotificationChannelsEnum channel;

    @NotBlank(message = "Template is required")
    private String template;

    @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[3-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
            message = "Invalid UUID format"
    )
    private String userId;

    private String subject;
    private String description;
    private String forName;
    private String forId;

    private Boolean isRead;

    private String expiresAt;

    // IMPORTANT: TS allowed string or list → in Java, use List<String>
    private java.util.List<String> recipients;

    private String redirectUrl;

    private String group;

    private Object context;
}
