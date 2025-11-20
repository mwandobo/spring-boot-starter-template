package com.bonnysimon.starter.features.mail;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/mail")
@RequiredArgsConstructor
public class TestEmailController {
    private final EmailService emailService;

    @GetMapping("/send-email")
    public String sendTestEmail() {
        emailService.sendSimpleEmail(
                "breezojr@gmail.com",
                "SpringBoot Email Test",
                "Hello from Spring Boot!"
        );

        return "Email sent!";
    }
}
