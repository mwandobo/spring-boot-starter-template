package com.example.super_start_web_api.features.user.model;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue
    private Long id;

    private String name;
    private String email;
    private String password;
}