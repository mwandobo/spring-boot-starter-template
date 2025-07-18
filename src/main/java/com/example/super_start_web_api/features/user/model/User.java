package com.example.super_start_web_api.features.user.model;
import com.example.super_start_web_api.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    private String name;
    private String email;
    private String password;
}