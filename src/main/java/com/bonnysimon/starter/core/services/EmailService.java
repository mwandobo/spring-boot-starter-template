package com.bonnysimon.starter.core.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    // Simple text email
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("noreply@yourdomain.com");

        mailSender.send(message);
    }

    // HTML email
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setFrom("noreply@yourdomain.com");

        mailSender.send(message);
    }

    // Template-based email (using Thymeleaf)
    public void sendTemplateEmail(String to, String subject, String templateName, Context context) throws MessagingException {
        String htmlContent = templateEngine.process(templateName, context);
        sendHtmlEmail(to, subject, htmlContent);
    }

    // Send welcome email with generated password
    public void sendWelcomeEmail(String to, String name, String plainPassword, String otp) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("password", plainPassword);
            context.setVariable("email", to);
            context.setVariable("otp", otp);


            sendTemplateEmail(
                    to,
                    "Welcome to Our Platform - Your Account Details",
                    "welcome-email",
                    context
            );
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send welcome email", e);
        }
    }
}