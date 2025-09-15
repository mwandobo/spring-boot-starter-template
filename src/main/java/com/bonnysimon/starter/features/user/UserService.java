package com.bonnysimon.starter.features.user;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.core.services.EmailConfigurationService;
import com.bonnysimon.starter.core.utils.RandomGenerator;
import com.bonnysimon.starter.features.notifications.dto.SendNotificationDTO;
import com.bonnysimon.starter.features.notifications.enums.NotificationChannelsEnum;
import com.bonnysimon.starter.features.notifications.enums.NotificationKeywordEnum;
import com.bonnysimon.starter.features.notifications.services.EmailSendingService;
import com.bonnysimon.starter.features.notifications.services.NotificationService;
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
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private final EmailSendingService emailSendingService;
    private final NotificationService notificationService;
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

//    @Transactional
//    public UserResponse create(CreateUserDTO dto) {
//        Optional<User> registeringUser = repository.findByEmail(dto.getEmail());
//        String password = randomGenerator.generateRandomPassword(8);
//
//        if(registeringUser.isPresent()) {
//            User presentUser = registeringUser.get();
//            Optional<UserOtp> otp = otpRepository.findByUserIdAndOtpType(presentUser.getId(), OtpType.OTP_REGISTERED );
//            if(otp.isPresent()) {
//                UserOtp userOtp = otp.get();
//                if(userOtp.isVerified()){
//                    throw new IllegalStateException("User Exists is already taken!");
//                }
//
//                if(userOtp.getExpiry().isAfter(Instant.now())) {
//                    throw new IllegalStateException("User Exists OTP not Verified Please Verify before it expires");
//                }
//                String newOtp = generateOtp();
//                userOtp.setOtp(newOtp);
//                userOtp.setExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));
//                otpRepository.save(userOtp);
//
//                Context context = new Context();
//                context.setVariable("name", dto.getName());
//                context.setVariable("password", password);
//                context.setVariable("email", dto.getEmail());
//                context.setVariable("otp", otp);
//
//                SendNotificationDTO sendNotificationDTO = getSendNotificationDTO(dto, context);
//                notificationService.sendNotification(sendNotificationDTO);
//                return new UserResponse( presentUser);
//            }
//        }
//
//        if (repository.existsByEmail(dto.getEmail())) {
//            throw new IllegalStateException("Email is already taken!");
//        }
//
//        User user = new User();
//        user.setEmail(dto.getEmail());
//        user.setPassword(passwordEncoder.encode(password));
//        user.setName(dto.getName());
//
//        if(dto.getRoleId() != null) {
//            Role role = roleRepository.findById(dto.getRoleId())
//                    .orElseThrow(() -> new IllegalArgumentException("Role Not found"));
//            user.setRole(role);
//        }
//
//       User savedUser = repository.save(user);
//
//        String otp = generateOtp();
//        UserOtp userOtp = new UserOtp();
//        userOtp.setUser(user);
//        userOtp.setOtp(otp);
//        userOtp.setExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));
//        otpRepository.save(userOtp);
//
//        // Send welcome email
//        try {
//
//            Context context = new Context();
//            context.setVariable("name", dto.getName());
//            context.setVariable("password", password);
//            context.setVariable("email", dto.getEmail());
//            context.setVariable("otp", otp);
//
//            SendNotificationDTO sendNotificationDTO = getSendNotificationDTO(dto, context);
//
//            notificationService.sendNotification(sendNotificationDTO);
//
//        } catch (Exception e) {
//            // Log the error but don't fail the user creation
//            log.error("Failed to send welcome email to: {}", dto.getEmail(), e);
//        }
//
//        return new UserResponse( savedUser);
//    }




    @Transactional
    public UserResponse create(CreateUserDTO dto) {
        Optional<User> existingUser = repository.findByEmail(dto.getEmail());
        String password = randomGenerator.generateRandomPassword(8);

        if (existingUser.isPresent()) {
            return handleExistingUser(existingUser.get(), dto, password);
        }

        if (repository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Email is already taken!");
        }

        User newUser = createNewUser(dto, password);
        repository.save(newUser);

        String otp = generateOtp();
        saveOtp(newUser, otp, OtpType.OTP_REGISTERED);

        sendWelcomeNotification(dto, newUser, password, otp);

        return new UserResponse(newUser);
    }

    private User createNewUser(CreateUserDTO dto, String password) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setPassword(passwordEncoder.encode(password));

        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found"));
            user.setRole(role);
        }

        return user;
    }

    private void saveOtp(User user, String otp, OtpType type) {
        UserOtp userOtp = new UserOtp();
        userOtp.setUser(user);
        userOtp.setOtp(otp);
        userOtp.setOtpType(type);
        userOtp.setExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));
        otpRepository.save(userOtp);
    }

    private UserResponse handleExistingUser(User user, CreateUserDTO dto, String password) {
        Optional<UserOtp> otpOptional = otpRepository.findByUserIdAndOtpType(user.getId(), OtpType.OTP_REGISTERED);
        if (otpOptional.isEmpty()) return new UserResponse(user);

        UserOtp userOtp = otpOptional.get();
        if (userOtp.isVerified()) throw new IllegalStateException("User Exists is already taken!");

        if (userOtp.getExpiry().isAfter(Instant.now())) {
            throw new IllegalStateException("User Exists OTP not Verified. Please verify before it expires");
        }

        String newOtp = generateOtp();
        userOtp.setOtp(newOtp);
        userOtp.setExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));
        otpRepository.save(userOtp);

        sendWelcomeNotification(dto, user, password, newOtp);
        return new UserResponse(user);
    }


    private void sendWelcomeNotification(CreateUserDTO dto, User user, String password, String otp) {
        try {
            Context context = new Context();
            context.setVariable("name", dto.getName());
            context.setVariable("password", password);
            context.setVariable("email", dto.getEmail());
            context.setVariable("otp", otp);

            SendNotificationDTO notificationDTO = createNotificationDTO(dto, context);
            notificationService.sendNotification(notificationDTO);
        } catch (Exception e) {
            log.error("Failed to send welcome notification to: {}", dto.getEmail(), e);
        }
    }

    private SendNotificationDTO createNotificationDTO(CreateUserDTO dto, Context context) {
        List<String> recipients = new ArrayList<>();
        recipients.add(dto.getEmail());

        SendNotificationDTO notificationDTO = new SendNotificationDTO();
        notificationDTO.setChannel(NotificationChannelsEnum.EMAIL);
        notificationDTO.setNotificationKeyword(NotificationKeywordEnum.WELCOME_MESSAGE);
        notificationDTO.setContext(context);
        notificationDTO.setRecipients(recipients);
        return notificationDTO;
    }



    private static SendNotificationDTO getSendNotificationDTO(CreateUserDTO dto, Context context) {
        List<String> recipients = new ArrayList<>();
        recipients.add(dto.getEmail());

        SendNotificationDTO sendNotificationDTO = new SendNotificationDTO();
        sendNotificationDTO.setChannel(NotificationChannelsEnum.EMAIL);
        sendNotificationDTO.setNotificationKeyword(NotificationKeywordEnum.WELCOME_MESSAGE);
        sendNotificationDTO.setContext(context);
        sendNotificationDTO.setRecipients(recipients);
        return sendNotificationDTO;
    }


    public UserResponse findOne(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return new UserResponse(user);
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
        return new UserResponse(savedUser);
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
                .orElseThrow(() -> new IllegalStateException("User not found with email: " + email));
        UserOtp userLoginOtp = otpRepository.findByUserIdAndOtpType(user.getId(), OtpType.OTP_REGISTERED)
                .orElseThrow(() -> new IllegalStateException("You can't change password, pending OTP verification"));
        if (!userLoginOtp.isVerified()) {
            throw new IllegalStateException("You can't change password, pending OTP verification");
        }

        // Generate recovery link with userId
        String link = recoveryUrl + "?userId=" + user.getId();

        // Find existing reset OTP if available
        UserOtp resetOtp = otpRepository.findByUserIdAndOtpType(user.getId(), OtpType.OTP_RESET_PASSWORD)
                .orElseGet(UserOtp::new); // if not present, create new one

        // Update or create reset OTP
        resetOtp.setUser(user);
        resetOtp.setLink(link);
        resetOtp.setOtpType(OtpType.OTP_RESET_PASSWORD);
        resetOtp.setExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));

        otpRepository.save(resetOtp);

        // Send recovery email
        emailSendingService.sendPasswordRecoveryEmail(email, user.getName(), link);
    }

    // Verify OTP + reset password
    @Transactional
    public void resetPassword(ChangePasswordDTO dto) {
        User user = repository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found with email: " + dto.getEmail()));

        UserOtp userLoginOtp = otpRepository.findByUserIdAndOtpType(user.getId(), OtpType.OTP_REGISTERED)
                .orElseThrow(() -> new IllegalStateException("Your OTP Registration was not Verified"));

        UserOtp userOtp = otpRepository.findByUserIdAndOtpType(user.getId(), OtpType.OTP_RESET_PASSWORD)
                .orElseThrow(() -> new IllegalStateException("There is The problem with the system. Data missing"));

        if(userOtp.getExpiry().isBefore(Instant.now())) {
            throw new IllegalStateException("Request is expired!");
        }

        // ✅ New checker: Verify old password
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalStateException("Old password is incorrect!");
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
                .orElseThrow(() -> new IllegalStateException("User not found with email: " + email));

        UserOtp userOtp = otpRepository.findByUserIdAndOtp(user.getId(), otp)
                .orElseThrow(() -> new IllegalStateException("Invalid OTP"));

        if (userOtp.getExpiry().isBefore(Instant.now())) {
            throw new IllegalArgumentException("OTP expired");
        }

        userOtp.setVerified(true);
        otpRepository.save(userOtp);
    }
}
