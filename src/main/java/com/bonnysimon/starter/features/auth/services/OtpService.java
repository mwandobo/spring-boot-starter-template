package com.bonnysimon.starter.features.auth.services;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final Logger logger = LoggerFactory.getLogger(OtpService.class);

    // Stores OTPs in memory: userId -> OTP details
    private final ConcurrentHashMap<Long, OtpDetails> otpStore = new ConcurrentHashMap<>();

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_SECONDS = 300; // 5 minutes

    private final Random random = new Random();

    // ---------- Generate OTP ----------
    public String generateOtp(Long userId) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        Instant expiryTime = Instant.now().plusSeconds(OTP_EXPIRY_SECONDS);

        otpStore.put(userId, new OtpDetails(otp, expiryTime));
        logger.info("Generated OTP for userId {}: {}", userId, otp); // For testing only, remove in prod

        return otp;
    }

    // ---------- Verify OTP ----------
    public boolean verifyOtp(Long userId, String otpCode) {
        OtpDetails otpDetails = otpStore.get(userId);

        if (otpDetails == null) {
            logger.warn("No OTP found for userId {}", userId);
            return false;
        }

        if (Instant.now().isAfter(otpDetails.getExpiryTime())) {
            otpStore.remove(userId);
            logger.warn("OTP expired for userId {}", userId);
            return false;
        }

        boolean valid = otpDetails.getOtp().equals(otpCode);
        if (valid) {
            otpStore.remove(userId); // OTP is single-use
            logger.info("OTP verified successfully for userId {}", userId);
        } else {
            logger.warn("Invalid OTP for userId {}", userId);
        }

        return valid;
    }

    // ---------- Send OTP ----------
    public void sendOtp(String destination, String otp) {
        // TODO: Replace with real email/SMS sending logic
        logger.info("Sending OTP {} to {}", otp, destination);
    }

    // ---------- Inner class to store OTP details ----------
    private static class OtpDetails {
        private final String otp;
        private final Instant expiryTime;

        public OtpDetails(String otp, Instant expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public Instant getExpiryTime() {
            return expiryTime;
        }
    }
}