package com.bonnysimon.starter.features.auth.services;

import com.bonnysimon.starter.core.constants.FrontEndRouteConstants;
import com.bonnysimon.starter.core.utils.JwtUtil;
import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.auth.dtos.LoginRequest;
import com.bonnysimon.starter.features.auth.dtos.LoginResponse;
import com.bonnysimon.starter.features.auth.dtos.RegisterRequest;
import com.bonnysimon.starter.features.auth.dtos.RegisterResponse;
import com.bonnysimon.starter.features.notification.NotificationService;
import com.bonnysimon.starter.features.notification.dto.SendNotificationDto;
import com.bonnysimon.starter.features.notification.enums.NotificationChannelsEnum;
import com.bonnysimon.starter.features.permission.Permission;
import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Data
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${spring.front.end.url}")
    private String frontEndUrl;

    public LoginResponse login(LoginRequest loginRequest) {
        try {
            logger.info("Attempting login for user: {}", loginRequest.getEmail());

            // Get user from DB
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new IllegalStateException("User not found"));

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            if (!Boolean.TRUE.equals(user.getIsOtpVerified())) {
                throw new IllegalStateException("User OTP is not verified yet");
            }


            logger.info("Authentication successful for user: {}", loginRequest.getEmail());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtil.generateToken(user.getEmail(), user.getId());
            logger.info("JWT generated for user: {}", loginRequest.getEmail());

            String roleName = user.getRole() != null ? user.getRole().getName() : null;
            Set<String> permissions = user.getRole() != null
                    ? user.getRole().getPermissions().stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet())
                    : Set.of();

            return new LoginResponse(jwt, user.getEmail(), roleName, permissions);

        } catch (Exception ex) {
            logger.error("Login failed for user: {}", loginRequest.getEmail(), ex);
            throw new IllegalStateException(ex.getMessage()); // or throw a custom AuthenticationException
        }
    }

    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalStateException("Email is already taken!");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setName(registerRequest.getName());

        userRepository.save(user);

        String otp = otpService.generateOtp(user.getId());
        user.setOtp(otp);
        user.setIsRecoveryRequested(true);

        userRepository.save(user);

        sendAuthNotification(user,otp,"send-otp", "Otp Verification");

        return new RegisterResponse(user.getName(), user.getEmail());
    }

    // --------- VERIFY OTP ---------
    public boolean verifyOtp(String email, String otpCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        boolean valid = otpService.verifyOtp(user.getId(), otpCode);
        if (valid) {
            logger.info("OTP verified for user: {}", email);
            user.setIsOtpVerified(true);
            userRepository.save(user);
        } else {
            logger.warn("OTP verification failed for user: {}", email);
        }
        return valid;
    }

    // --------- PASSWORD RECOVERY REQUEST ---------
    public void passwordRecoveryRequest(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        String otp = otpService.generateOtp(user.getId());

        user.setIsRecoveryRequested(true);
        user.setOtp(otp);
        userRepository.save(user);

        sendAuthNotification(user,otp,"password-change", "Request For Password Change");

        logger.info("Password recovery OTP sent for user: {}", email);
    }

    // --------- CHANGE PASSWORD ---------
    public void changePassword(String email, String oldPassword, String newPassword) {

        logger.info("Password recovery OTP sent for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (!user.getIsRecoveryRequested()) {
            throw new IllegalStateException("No Request For Change Password Found");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalStateException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setIsRecoveryRequested(false);
        userRepository.save(user);

        logger.info("Password changed successfully for user: {}", email);
    }



//    @Transactional
//    public void sendAuthNotification(User user, String otp, String template, String subject) throws MessagingException {
//
//        log.info("Approval level passed level={}", toJson(user));
//
//        String redirectUrl = frontEndUrl + "/"
//                + FrontEndRouteConstants.CREATE_APPROVAL_LEVEL_REDIRECT_URL;
//
//        Map<String, Object> context = new HashMap<>();
//
//        context.put("otp", otp);
//        context.put("expiryMinutes", 5);
//        context.put("year", Year.now().getValue());
//
//
//        List<String> recipients = new ArrayList<>();
//        recipients.add(user.getEmail());
//
//
//        // Build notification DTO
//        SendNotificationDto dto = new SendNotificationDto();
//        dto.setChannel(NotificationChannelsEnum.EMAIL);
//        dto.setRecipients(recipients);
//        dto.setForName(user.getName());
//        dto.setForId(user.getId());
//        dto.setContext(context);
//        dto.setTemplate(template);
//        dto.setSubject(subject);
//        dto.setDescription(subject);
//        dto.setRedirectUrl(redirectUrl);
//
//        // Send notification
//        notificationService.sendNotification(dto);
//    }

    public void sendAuthNotification(User user, String otp, String template, String subject) {

        try {
            log.info("Auth notification for user={}", toJson(user));

            String redirectUrl = frontEndUrl + "/"
                    + FrontEndRouteConstants.CREATE_APPROVAL_LEVEL_REDIRECT_URL;

            Map<String, Object> context = new HashMap<>();

            if (otp != null && !otp.isBlank()) {
                context.put("otp", otp);
            }

            if ("password-change".equals(template)) {
                context.put("name", user.getName());
                context.put("expiryMinutes", 5);
                context.put("year", Year.now().getValue());
            }

            context.put("expiryMinutes", 5);
            context.put("year", Year.now().getValue());

            List<String> recipients = List.of(user.getEmail());

            SendNotificationDto dto = new SendNotificationDto();
            dto.setChannel(NotificationChannelsEnum.EMAIL);
            dto.setRecipients(recipients);
            dto.setForName(user.getName());
            dto.setForId(user.getId());
            dto.setContext(context);
            dto.setTemplate(template);
            dto.setSubject(subject);
            dto.setDescription(subject);
            dto.setRedirectUrl(redirectUrl);

            notificationService.sendNotification(dto);

        } catch (Exception e) {
            log.error("Failed to send auth notification for user={}", user.getEmail(), e);
        }
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

}