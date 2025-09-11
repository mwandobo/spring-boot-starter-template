package com.bonnysimon.starter.features.user.dto;

import lombok.Data;

@Data
public class ChangePasswordDTO {
    private String email;
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;
}
