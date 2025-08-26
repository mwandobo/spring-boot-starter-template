package com.bonnysimon.starter.features.user.model;
import com.bonnysimon.starter.core.entity.BaseEntity;
import com.bonnysimon.starter.features.role.Role;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    private String name;
    private String email;
    private String password;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;
}