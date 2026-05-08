package com.bonnysimon.starter.features.auth.dtos;

import com.bonnysimon.starter.features.notification.dto.NotificationResponseDto;
import com.bonnysimon.starter.features.user.UserEntity;
import com.bonnysimon.starter.features.user.dto.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class LoginResponse {

    private String access_token;
    private UserResponseDTO user;
    private List<NotificationResponseDto> notifications;
}