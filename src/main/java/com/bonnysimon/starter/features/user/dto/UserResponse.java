package com.bonnysimon.starter.features.user.dto;

import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String password;
    private Role role;

    // Additional constructor for easy mapping
    public UserResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
    }

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                null ,// Password is null for GET requests
                user.getRole()
        );
    }
}
