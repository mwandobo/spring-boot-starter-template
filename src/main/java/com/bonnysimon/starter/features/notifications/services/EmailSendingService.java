package com.bonnysimon.starter.features.notifications.services;

import com.bonnysimon.starter.core.services.EmailConfigurationService;
import com.bonnysimon.starter.features.notifications.enums.NotificationKeywordEnum;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailSendingService {
    private final EmailConfigurationService emailConfigurationService;

    // Generic method
    public void sendEmail(Context context, List<String> recipients, NotificationKeywordEnum keyword) {
        try {
            for (String to : recipients) {
                emailConfigurationService.sendTemplateEmail(
                        to,
                        getSubjectByKeyword(keyword),
                        getTemplateByKeyword(keyword),
                        context
                );
            }
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email for keyword: " + keyword, e);
        }
    }

    private String getSubjectByKeyword(NotificationKeywordEnum keyword) {
        switch (keyword) {
            case WELCOME_MESSAGE:
                return "Welcome to Our Platform - Your Account Details";
            case RECOVERY_MESSAGE:
                return "Password Reset";
            default:
                return "Notification from Our Platform";
        }
    }

    private String getTemplateByKeyword(NotificationKeywordEnum keyword) {
        switch (keyword) {
            case WELCOME_MESSAGE:
                return "welcome-email";
            case RECOVERY_MESSAGE:
                return "password-recovery-email";
            default:
                return "generic-email";
        }
    }
}
