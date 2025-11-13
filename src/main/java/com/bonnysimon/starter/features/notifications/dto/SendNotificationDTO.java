package com.bonnysimon.starter.features.notifications.dto;

import com.bonnysimon.starter.features.notifications.enums.NotificationChannelsEnum;
import com.bonnysimon.starter.features.notifications.enums.NotificationKeywordEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.thymeleaf.context.Context;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationDTO {
    private NotificationChannelsEnum channel;
    private NotificationKeywordEnum notificationKeyword;
    private Context context;
    private List<String> recipients;
}