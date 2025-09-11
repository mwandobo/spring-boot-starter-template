package com.bonnysimon.starter.features.user;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.core.services.EmailService;
import com.bonnysimon.starter.core.utils.RandomGenerator;
import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.role.RoleRepository;
import com.bonnysimon.starter.features.user.dto.ChangePasswordDTO;
import com.bonnysimon.starter.features.user.dto.CreateUserDTO;
import com.bonnysimon.starter.features.user.dto.UserResponse;
import com.bonnysimon.starter.features.user.enums.OtpType;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.model.UserOtp;
import com.bonnysimon.starter.features.user.repository.UserOtpRepository;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
// other imports...

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RandomGenerator randomGenerator;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final UserOtpRepository otpRepository;

    @Value("${spring.mail.recovery.url}")
    private String recoveryUrl;


    public PaginationResponse<UserResponse> findAll(PaginationRequest pagination, String search) {
        Specification<User> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        Page<User> users = repository.findAll(spec, pagination.toPageable());

        Page<UserResponse> userResponses = users.map(UserResponse::fromEntity);

        return PaginationResponse.of(userResponses);
    }

    @Transactional
    public UserResponse create(CreateUserDTO dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Email is already taken!");
        }

        String password = randomGenerator.generateRandomPassword(8);

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setName(dto.getName());

        if(dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role Not found"));
            user.setRole(role);
        }

       User savedUser = repository.save(user);

        String otp = generateOtp();
        UserOtp userOtp = new UserOtp();
        userOtp.setUser(user);
        userOtp.setOtp(otp);
        userOtp.setExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));
        otpRepository.save(userOtp);

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(dto.getEmail(), dto.getName(), password, generateOtp());
        } catch (Exception e) {
            // Log the error but don't fail the user creation
            log.error("Failed to send welcome email to: {}", dto.getEmail(), e);
        }

        return new UserResponse( savedUser,password);
    }

    public UserResponse findOne(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        return new UserResponse(user, null);
    }

    @Transactional
    public UserResponse update(Long id, CreateUserDTO dto) {

       User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setName(dto.getName());

        if(dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role Not found"));
            user.setRole(role);
        }

        User savedUser = repository.save(user);

        return new UserResponse(savedUser, null);
    }

    public void delete(Long id, boolean soft) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (soft) {
            user.setDeleted(true); // soft delete flag from BaseEntity
            repository.save(user);
        } else {
            repository.delete(user);
        }
    }


    // Request password reset (send OTP)
    @Transactional
    public void requestPasswordReset(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        String link = recoveryUrl + "?userId=" + user.getId();
        UserOtp userOtp = new UserOtp();
        userOtp.setUser(user);
        userOtp.setLink(link);
        userOtp.setOtpType(OtpType.OTP_RESET_PASSWORD);
        userOtp.setExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));
        otpRepository.save(userOtp);

        emailService.sendPasswordRecoveryEmail(email, user.getName(), link);
    }

    // Verify OTP + reset password
    @Transactional
    public void resetPassword(ChangePasswordDTO dto) {
        User user = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + dto.getEmail()));

        UserOtp userOtp = otpRepository.findByUserIdAndOtpType(user.getId(), OtpType.OTP_RESET_PASSWORD)
                .orElseThrow(() -> new IllegalArgumentException("There is The problem with the system. Data missing"));

        if(userOtp.getExpiry().isBefore(Instant.now())) {
            throw new IllegalStateException("Request is expired!");
        }

        if(!dto.getConfirmNewPassword().equals(dto.getNewPassword())) {
            throw new IllegalStateException("New passwords do not match!");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        repository.save(user);
    }

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000)); // 6-digit OTP
    }

    // Verify OTP + reset password
    @Transactional
    public void verifyOtp(String email, String otp) {

        User user = repository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        UserOtp userOtp = otpRepository.findByUserIdAndOtp(user.getId(), otp)
                .orElseThrow(() -> new IllegalArgumentException("Invalid OTP"));

        if (userOtp.getExpiry().isBefore(Instant.now())) {
            throw new IllegalArgumentException("OTP expired");
        }

        userOtp.setVerified(true);
        otpRepository.save(userOtp);
    }




}
