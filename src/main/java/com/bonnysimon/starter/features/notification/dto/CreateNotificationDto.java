package com.bonnysimon.starter.features.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationDto {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "forName is required")
    private String forName;

    @NotBlank(message = "forId is required")
    private String forId;

    private Boolean isRead; // optional

    private String expiresAt; // optional

    private Long userId;

    private Long recipientId;

    private String redirectUrl;

    private String group;
}
