package com.bonnysimon.starter.features.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("Mwalimu Commercial Bank <no-reply@mwalimucommercialbank.co.tz>");
        mailSender.send(message);
    }

    public void sendHtmlEmail(          EmailPayload emailPayload) throws MessagingException {
//        public void sendHtmlEmail(          String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        String[] recipients = emailPayload.getTo().toArray(new String[0]);

        helper.setTo(recipients);
        helper.setSubject(emailPayload.getSubject());
        helper.setText(emailPayload.getTemplate(), true);
        helper.setFrom("Mwalimu Commercial Bank <no-reply@mwalimucommercialbank.co.tz>");
        mailSender.send(mimeMessage);
    }
}
