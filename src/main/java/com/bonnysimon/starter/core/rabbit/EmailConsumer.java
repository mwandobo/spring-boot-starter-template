package com.bonnysimon.starter.core.rabbit;

import com.bonnysimon.starter.core.config.RabbitConfig;
import com.bonnysimon.starter.features.mail.EmailPayload;
import com.bonnysimon.starter.features.mail.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;           // ← IMPORTANT

@Slf4j
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

    @RabbitListener(queues = RabbitConfig.DLQ)
    public void processDeadLetter(EmailPayload payload, Message message) {
        // Log full message + headers (you can see retry count, exception, etc.)
        log.error("Message moved to DLQ after retries: {}", payload);
        log.error("Headers: {}", message.getMessageProperties().getHeaders());

        // Optional: send alert (Slack, email to admin), store in DB, etc.
        // Do NOT rethrow exception here unless you want infinite loop
    }
}