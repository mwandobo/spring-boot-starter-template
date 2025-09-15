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

    // Simple text email

//    // Send welcome email with generated password
//    public void sendWelcomeEmail(Context context, List<String> recipients) {
////    public void sendWelcomeEmail(String to, String name, String plainPassword, String otp) {
//        try {
////            Context context = new Context();
////            context.setVariable("name", name);
////            context.setVariable("password", plainPassword);
////            context.setVariable("email", to);
////            context.setVariable("otp", otp);
//
//
//
//
//            emailConfigurationService.sendTemplateEmail(
//                    to,
//                    "Welcome to Our Platform - Your Account Details",
//                    "welcome-email",
//                    context
//            );
//
//
//        } catch (MessagingException e) {
//            throw new RuntimeException("Failed to send welcome email", e);
//        }
//    }


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


    public void sendPasswordRecoveryEmail(String to, String name, String link) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("email", to);
            context.setVariable("link", link);


            emailConfigurationService.sendTemplateEmail(
                    to,
                    "Password Reset",
                    "password-recovery-email",
                    context
            );
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }
}