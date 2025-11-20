package com.bonnysimon.starter.features.notification;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.notification.dto.CreateNotificationDto;
import com.bonnysimon.starter.features.notification.dto.NotificationResponseDto;
import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.role.dto.CreateRoleRequest;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository repository;
    private final UserRepository userRepository;

    private NotificationResponseDto convertToResponseDto(NotificationEntity entity) {
        return NotificationResponseDto.fromEntity(entity);
    }
//    public PaginationResponse<NotificationEntity> findAll(PaginationRequest pagination, String search) {
//        // Base spec: filter out deleted notifications
//        Specification<NotificationEntity> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));
//
//        // Optional search filter (case-insensitive)
//        if (search != null && !search.trim().isEmpty()) {
//            String likePattern = "%" + search.trim().toLowerCase() + "%";
//            spec = spec.and((root, query, cb) ->
//                    cb.or(
//                            cb.like(cb.lower(root.get("title")), likePattern),
//                            cb.like(cb.lower(root.get("description")), likePattern)
//                    )
//            );
//        }
//
//        // Execute paginated query
//        Page<NotificationEntity> notificationsPage = repository.findAll(spec, pagination.toPageable());
//
//        // Wrap in PaginationResponse
//        return PaginationResponse.of(notificationsPage);
//    }

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

//
//    public Role create(CreateRoleRequest request) {
//        // Check if role already exists
//        roleRepository.findByName(request.getName())
//                .ifPresent(r -> {
//                    throw new IllegalArgumentException("Role with name '" + request.getName() + "' already exists");
//                });
//
//        // Map DTO -> Entity
//        Role role = new Role();
//        role.setName(request.getName());
//
//        return roleRepository.save(role);
//    }
}
