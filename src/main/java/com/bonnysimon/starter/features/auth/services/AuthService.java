package com.bonnysimon.starter.features.auth.services;

import com.bonnysimon.starter.core.utils.JwtUtil;
import com.bonnysimon.starter.features.auth.dtos.LoginRequest;
import com.bonnysimon.starter.features.auth.dtos.LoginResponse;
import com.bonnysimon.starter.features.auth.dtos.RegisterRequest;
import com.bonnysimon.starter.features.auth.dtos.RegisterResponse;
import com.bonnysimon.starter.features.permission.Permission;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import lombok.Data;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Data
public class AuthService {
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);

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

        return new RegisterResponse(user.getName(), user.getEmail());
    }

    // --------- VERIFY OTP ---------
    public boolean verifyOtp(String email, String otpCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        boolean valid = otpService.verifyOtp(user.getId(), otpCode);
        if (valid) {
            logger.info("OTP verified for user: {}", email);
        } else {
            logger.warn("OTP verification failed for user: {}", email);
        }
        return valid;
    }

    // --------- CHANGE PASSWORD ---------
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalStateException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        logger.info("Password changed successfully for user: {}", email);
    }

    // --------- PASSWORD RECOVERY REQUEST ---------
    public void passwordRecoveryRequest(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Generate OTP for password reset
        String otp = otpService.generateOtp(user.getId());

        // Send OTP to user email or phone (hypothetical method)
        otpService.sendOtp(user.getEmail(), otp);

        logger.info("Password recovery OTP sent for user: {}", email);
    }
}