package com.bonnysimon.starter.features.user;
import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.approval.utils.ApprovalStatusUtil;
import com.bonnysimon.starter.features.user.dto.ChangePasswordDTO;
import com.bonnysimon.starter.features.user.dto.CreateUserDTO;
import com.bonnysimon.starter.features.user.dto.UserResponse;
import com.bonnysimon.starter.features.user.dto.VerifyOtpDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {
    private final UserService service;
    private final ApprovalStatusUtil approvalStatusUtil;

    @GetMapping
    public ApiResponse<PaginationResponse<UserResponse>> getAllUsers(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        PaginationResponse<UserResponse> users = service.findAll(pagination, search);
        boolean hasApprovalMode = approvalStatusUtil.hasApprovalMode("User");
        return ApiResponse.success(users, hasApprovalMode);
    }

    @PostMapping
    public ApiResponse<UserResponse> create(@RequestBody CreateUserDTO request) {
        return ApiResponse.success(service.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> findOne(
            @PathVariable Long id
    ) {
        return ApiResponse.success(service.findOne(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> update(
            @PathVariable Long id,
            @RequestBody CreateUserDTO request
    ) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @RequestParam(name = "soft", defaultValue = "false") boolean soft
    ) {
        service.delete(id, soft);
        return ApiResponse.success(null);
    }

    @GetMapping("/request-password-reset")
    public ApiResponse<String> requestPasswordReset(@RequestParam String email) {
        service.requestPasswordReset(email);
        return ApiResponse.success("Password reset requested. Please check your email.");
    }

    @PostMapping("/verify-otp")
    public ApiResponse<String> verifyOtp(@RequestBody VerifyOtpDTO request) {
        service.verifyOtp(request.getEmail(), request.getOtp());
        return ApiResponse.success( "OTP verified successfully");
    }

    @PostMapping("/change-password")
    public ApiResponse<String> changePassword(@RequestBody ChangePasswordDTO request) {
        service.resetPassword(request);
        return ApiResponse.success( "Password changed successfully");
    }
}