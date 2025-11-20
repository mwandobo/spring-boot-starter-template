package com.bonnysimon.starter.features.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;


    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("Mwalimu Commercial Bank <no-reply@mwalimucommercialbank.co.tz>");
        mailSender.send(message);
    }

    public void sendHtmlEmail(          EmailPayload emailPayload) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        String[] recipients = emailPayload.getTo().toArray(new String[0]);

        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(emailPayload.getContext());

        String htmlContent = templateEngine.process(
                "email/" + emailPayload.getTemplate(),
                thymeleafContext
        );

        helper.setTo(recipients);
        helper.setSubject(emailPayload.getSubject());
        helper.setText(htmlContent, true);
        helper.setFrom("Mwalimu Commercial Bank <no-reply@mwalimucommercialbank.co.tz>");
        mailSender.send(mimeMessage);
    }
}
