package com.bonnysimon.starter.core.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE = "notification.email.queue";
    public static final String DLQ = QUEUE + ".dlq";           // Dead Letter Queue
    public static final String DLX = "dlx.notification.email"; // Dead Letter Exchange

//    @Bean
//    public Queue notificationQueue() {
//        return new Queue(QUEUE);
//    }
//
//    @Bean
//    public Jackson2JsonMessageConverter jsonMessageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)        // ← key
                .withArgument("x-dead-letter-routing-key", DLQ)     // ← key
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ, true); // durable
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DLQ);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}