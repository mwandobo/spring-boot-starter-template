package com.bonnysimon.starter.features.notification;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.mail.EmailPayload;
import com.bonnysimon.starter.features.mail.EmailService;
import com.bonnysimon.starter.features.notification.dto.CreateNotificationDto;
import com.bonnysimon.starter.features.notification.dto.NotificationResponseDto;
import com.bonnysimon.starter.features.notification.dto.SendNotificationDto;
import com.bonnysimon.starter.features.notification.enums.NotificationChannelsEnum;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private NotificationResponseDto convertToResponseDto(NotificationEntity entity) {
        return NotificationResponseDto.fromEntity(entity);
    }

    public PaginationResponse<NotificationResponseDto> findAll(PaginationRequest pagination, String search) {
        // Base spec: filter out deleted notifications
        Specification<NotificationEntity> spec = getEntitySpecification(search);
        // Execute paginated query
        Page<NotificationEntity> notificationsPage = repository.findAll(spec, pagination.toPageable());

        // Convert entities to DTOs
        List<NotificationResponseDto> dtoList = notificationsPage.getContent()
                .stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());

        // Create new page with DTOs
        Page<NotificationResponseDto> dtoPage = new PageImpl<>(
                dtoList,
                notificationsPage.getPageable(),
                notificationsPage.getTotalElements()
        );

        // Wrap in PaginationResponse
        return PaginationResponse.of(dtoPage);
    }

    private static Specification<NotificationEntity> getEntitySpecification(String search) {
        Specification<NotificationEntity> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

        // Optional search filter (case-insensitive)
        if (search != null && !search.trim().isEmpty()) {
            String likePattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), likePattern),
                            cb.like(cb.lower(root.get("description")), likePattern)
                    )
            );
        }
        return spec;
    }

    public NotificationEntity create(CreateNotificationDto dto) {
        // Fetch user if userId provided
        User user = null;
        if (dto.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(dto.getUserId());
            if (userOpt.isPresent()) {
                user = userOpt.get();
            } else {
                throw new RuntimeException("User not found with id: " + dto.getUserId());
            }
        }

        // Fetch notifiedPersonnel if recipientId provided
        User notifiedPersonnel = null;
        if (dto.getRecipientId() != null) {
            Optional<User> notifiedOpt = userRepository.findById(dto.getRecipientId());
            if (notifiedOpt.isPresent()) {
                notifiedPersonnel = notifiedOpt.get();
            } else {
                throw new RuntimeException("Recipient not found with id: " + dto.getRecipientId());
            }
        }

        // Map DTO to entity
        NotificationEntity notification = new NotificationEntity();
        notification.setTitle(dto.getTitle());
        notification.setDescription(dto.getDescription());
        notification.setForName(dto.getForName());
        notification.setForId(dto.getForId());
        notification.setRead(dto.getIsRead() != null ? dto.getIsRead() : false);
        notification.setExpiresAt(dto.getExpiresAt());
        notification.setRedirectUrl(dto.getRedirectUrl());
        notification.setGroupName(dto.getGroup()); // use renamed column from previous fix
        notification.setUser(user);
        notification.setNotifiedPersonnel(notifiedPersonnel);

        // Save entity
        NotificationEntity saved = repository.save(notification);

        // Convert to response DTO
        return saved;
    }

    public String sendNotification(SendNotificationDto dto) throws MessagingException {

        NotificationChannelsEnum channel = dto.getChannel();
        String template = dto.getTemplate();
        List<String> recipients = dto.getRecipients();
        Long userId = dto.getUserId();
        String subject = dto.getSubject();
        Map<String, Object> context = dto.getContext();
        String description = dto.getDescription();
        String forName = dto.getForName();
        Long forId = dto.getForId();
        String redirectUrl = dto.getRedirectUrl();

        // 1. SEND EMAIL IF CHANNEL IS EMAIL
        if (NotificationChannelsEnum.EMAIL.name().equalsIgnoreCase(String.valueOf(channel))) {

            EmailPayload emailPayload = EmailPayload.builder()
                    .to(recipients)
                    .subject(subject)
                    .template(template)
                    .context(context)
                    .build();

            emailService.sendHtmlEmail(emailPayload); // async or sync based on your implementation
        }

        // 2. CREATE NOTIFICATION RECORDS FOR EACH RECIPIENT
        for (String recipientId : recipients) {

            CreateNotificationDto createDto = new CreateNotificationDto();
            createDto.setTitle(subject);
            createDto.setDescription(description);
            createDto.setForName(forName);
            createDto.setForId(forId);
            createDto.setUserId(dto.getUserId());
//            createDto.setRecipientId(Long.valueOf(recipientId));
            createDto.setRedirectUrl(redirectUrl);
            createDto.setGroup(""); // same behavior as NestJS

            this.create(createDto);
        }

        return "notification sent successfully";
    }


}
