package com.bonnysimon.starter.features.notifications.dto;

import com.bonnysimon.starter.features.notifications.enums.NotificationChannelsEnum;
import com.bonnysimon.starter.features.notifications.enums.NotificationKeywordEnum;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.context.Context;

import java.util.List;

@RequiredArgsConstructor
@Data
public class SendNotificationDTO {
    private NotificationChannelsEnum channel;
    private NotificationKeywordEnum notificationKeyword;
    private List<String> recipients;
    private Context context;
    private String subject;
    private String message;
}
