package com.bonnysimon.starter.features.notifications.services;

import com.bonnysimon.starter.features.notifications.dto.SendNotificationDTO;
import com.bonnysimon.starter.features.notifications.enums.NotificationChannelsEnum;
import com.bonnysimon.starter.features.notifications.enums.NotificationKeywordEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {
    private final EmailSendingService emailSendingService;

    public void sendNotification(SendNotificationDTO dto) {
        if (dto == null) {
            log.warn("SendNotificationDTO is null");
            throw new IllegalArgumentException("Notification DTO cannot be null");
        }

        NotificationChannelsEnum channel = dto.getChannel();
        NotificationKeywordEnum keyword = dto.getNotificationKeyword();
        Context context = dto.getContext();
        List<String> recipients = dto.getRecipients();

        log.info("Preparing to send notification");
        log.info("Channel: {}", channel);
        log.info("Keyword: {}", keyword);
        log.info("Recipients: {}", recipients);

        if (channel == null) {
            log.error("Notification channel is not set for DTO: {}", dto);
            throw new IllegalStateException("Notification channel is not set");
        }

        switch (channel) {
            case EMAIL -> {
                if (recipients == null || recipients.isEmpty()) {
                    log.warn("No recipients provided for EMAIL notification with keyword: {}", keyword);
                    return;
                }
                log.info("Sending EMAIL notification with keyword: {} to recipients: {}", keyword, recipients);
                try {
                    if (keyword == NotificationKeywordEnum.WELCOME_MESSAGE) {
                        emailSendingService.sendWelcomeEmail(context, recipients);
                        log.info("WELCOME_MESSAGE email sent successfully to recipients: {}", recipients);
                    }else if(keyword == NotificationKeywordEnum.RECOVERY_MESSAGE){
                        emailSendingService.sendPasswordRecoveryEmail(context, recipients);
                        log.info("RECOVERY_MESSAGE email sent successfully to recipients: {}", recipients);
                    }
                    else {
                        log.warn("Email keyword {} is not handled yet", keyword);
                    }
                } catch (Exception e) {
                    log.error("Failed to send EMAIL notification for keyword: {} to recipients: {}", keyword, recipients, e);
                }
            }
            default -> log.warn("Notification channel {} is not implemented yet", channel);
        }
    }
}
