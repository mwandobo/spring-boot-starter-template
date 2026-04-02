package com.bonnysimon.starter.features.auth.dtos;

import com.bonnysimon.starter.features.notification.NotificationEntity;
import com.bonnysimon.starter.features.notification.dto.NotificationResponseDto;
import com.bonnysimon.starter.features.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class LoginResponse {

    private String access_token;
    private User user;
    private List<NotificationResponseDto> notifications;
}