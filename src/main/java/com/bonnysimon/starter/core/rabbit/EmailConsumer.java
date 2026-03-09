package com.bonnysimon.starter.core.rabbit;

import com.bonnysimon.starter.core.config.RabbitConfig;
import com.bonnysimon.starter.features.mail.EmailPayload;
import com.bonnysimon.starter.features.mail.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;

@Service
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailService mailSenderService;


    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void processEmailQueue(EmailPayload emailPayload) {
        try {
            mailSenderService.sendHtmlEmail(emailPayload);
        } catch (MessagingException e) {
            // Log and handle error (retry can be added later)
            e.printStackTrace();
        }
    }
}