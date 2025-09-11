package com.bonnysimon.starter.features.user.model;

import com.bonnysimon.starter.core.entity.BaseEntity;
import com.bonnysimon.starter.features.user.enums.OtpType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "user_otps")
public class UserOtp extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String otp;

    private String link;

    @Enumerated(EnumType.STRING)
    @Column(name = "otp_type")
    private OtpType otpType = OtpType.OTP_REGISTERED;

    private Instant expiry; // expires in e.g. 10 minutes

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    private boolean verified = false; // has the OTP been verified?

    private boolean passwordChanged = false;
}
