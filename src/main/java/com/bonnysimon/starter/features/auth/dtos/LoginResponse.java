package com.bonnysimon.starter.features.auth.dtos;

import java.util.Set;

public class LoginResponse {
    private String token;
    private String email;
    private String role;
    private Set<String> permissions;

    public LoginResponse(String token, String email, String role, Set<String> permissions) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.permissions = permissions;
    }

    // Getters & Setters
    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Set<String> getPermissions() { return permissions; }
}
