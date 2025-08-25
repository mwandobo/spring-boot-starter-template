package com.bonnysimon.starter.features.user.model;
import com.bonnysimon.starter.core.entity.BaseEntity;
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