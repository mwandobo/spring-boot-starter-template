package com.bonnysimon.starter.features.user.repository;

import com.bonnysimon.starter.features.user.model.UserOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserOtpRepository extends JpaRepository<UserOtp, Long> {
    Optional<UserOtp> findByUserIdAndOtp(Long userId, String otp);
}
