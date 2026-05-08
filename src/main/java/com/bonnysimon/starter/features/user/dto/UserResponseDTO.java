package com.bonnysimon.starter.features.user.dto;

import com.bonnysimon.starter.features.role.dto.RoleResponseDTO;
import com.bonnysimon.starter.features.user.UserEntity;
import java.time.format.DateTimeFormatter;
import lombok.Data;
@Data
public class UserResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String email;
    private RoleResponseDTO role;
    private String roleName;
    private Boolean isRecoveryRequested;
    private Boolean isOtpVerified;
    private String phone;
    private String otp;
    private String password;
    private String approvalStatus;
    private String createdAt;
    private String updatedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static  UserResponseDTO fromEntity( UserEntity user) {
            UserResponseDTO dto = new UserResponseDTO();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setDescription(user.getDescription());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole() != null ? RoleResponseDTO.fromEntity(user.getRole()) : null);
              dto.setRoleName(user.getRole() != null ? user.getRole().getName() : null);
            dto.setIsRecoveryRequested(user.getIsRecoveryRequested());
            dto.setIsOtpVerified(user.getIsOtpVerified());
            dto.setPhone(user.getPhone());
            dto.setOtp(user.getOtp());
            dto.setPassword(user.getPassword());
            dto.setUpdatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null);
            dto.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
            return dto;
        }
}
