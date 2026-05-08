package com.bonnysimon.starter.features.user;

import com.bonnysimon.starter.core.entity.BaseEntity;
import com.bonnysimon.starter.features.role.RoleEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "user")
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private RoleEntity role;


    @Column(name = "password", nullable = true)
    private String password;


    @Column(name = "otp", nullable = true)
    private String otp;


    @Column(name = "phone", nullable = true)
    private String phone;


    @Column(name = "isotpverified", nullable = true)
    private Boolean isOtpVerified = false;


    @Column(name = "isrecoveryrequested", nullable = true)
    private Boolean isRecoveryRequested;


    @Column(name = "email", nullable = true)
    private String email;

}
