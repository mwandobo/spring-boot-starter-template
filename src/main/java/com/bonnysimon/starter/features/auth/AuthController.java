package com.bonnysimon.starter.features.auth;

import com.bonnysimon.starter.features.auth.dtos.*;
import com.bonnysimon.starter.features.auth.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public  ResponseEntity<RegisterResponse>  register(@RequestBody RegisterRequest registerRequest) {
        RegisterResponse response =  authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    // --------- VERIFY OTP ---------
    @PostMapping("/verify-otp")
    public ResponseEntity<OtpVerificationResponse> verifyOtp(
            @RequestParam String email,
            @RequestBody OtpVerificationRequest request
    ) {
        boolean isValid = authService.verifyOtp(email, request.getOtp());
        OtpVerificationResponse response = new OtpVerificationResponse(isValid);
        return ResponseEntity.ok(response);
    }

    // --------- CHANGE PASSWORD ---------
    @PostMapping("/change-password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request.getEmail(), request.getOldPassword(), request.getNewPassword());
        ChangePasswordResponse response = new ChangePasswordResponse("Password changed successfully");
        return ResponseEntity.ok(response);
    }

    // --------- PASSWORD RECOVERY REQUEST ---------
    @GetMapping("/password-recovery-request")
    public ResponseEntity<PasswordRecoveryResponse> passwordRecoveryRequest(
            @RequestParam String email
    ) {
        authService.passwordRecoveryRequest(email);
        PasswordRecoveryResponse response = new PasswordRecoveryResponse("OTP sent for password recovery");
        return ResponseEntity.ok(response);
    }
}