package com.bonnysimon.starter.features.notifications.services;

import com.bonnysimon.starter.core.services.EmailConfigurationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailSendingService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailConfigurationService emailConfigurationService;


// Send welcome email with generated password to multiple recipients
public void sendWelcomeEmail(Context context, List<String> recipients) {
    try {
        for (String to : recipients) {
            emailConfigurationService.sendTemplateEmail(
                    to,
                    "Welcome to Our Platform - Your Account Details",
                    "welcome-email",
                    context
            );
        }
    } catch (MessagingException e) {
        throw new RuntimeException("Failed to send welcome email", e);
    }
}

        public void sendPasswordRecoveryEmail(Context context,List<String> recipients) {
        try {

            for (String to : recipients) {
                emailConfigurationService.sendTemplateEmail(
                        to,
                        "Password Reset",
                        "password-recovery-email",
                        context
                );
            }
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }
}