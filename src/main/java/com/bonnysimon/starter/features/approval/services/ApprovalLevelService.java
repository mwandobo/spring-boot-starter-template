package com.bonnysimon.starter.features.approval.services;

import com.bonnysimon.starter.core.constants.FrontEndRouteConstants;
import com.bonnysimon.starter.core.dto.PagedResponse;
import com.bonnysimon.starter.core.dto.PaginationDto;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.services.CurrentUserService;
import com.bonnysimon.starter.features.approval.dto.ApprovalAwareDTO;
import com.bonnysimon.starter.features.approval.dto.ApprovalLevelRequestDTO;
import com.bonnysimon.starter.features.approval.dto.ApprovalLevelResponseDTO;
import com.bonnysimon.starter.features.approval.entity.ApprovalAction;
import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.approval.entity.UserApproval;
import com.bonnysimon.starter.features.approval.enums.ApprovalActionCreationTypeEnum;
import com.bonnysimon.starter.features.approval.enums.ApprovalActionEnum;
import com.bonnysimon.starter.features.approval.repository.ApprovalActionRepository;
import com.bonnysimon.starter.features.approval.repository.ApprovalLevelRepository;
import com.bonnysimon.starter.features.approval.repository.UserApprovalRepository;
import com.bonnysimon.starter.features.approval.util.ApprovalStatusUtil;
import com.bonnysimon.starter.features.notification.NotificationService;
import com.bonnysimon.starter.features.notification.dto.SendNotificationDto;
import com.bonnysimon.starter.features.notification.enums.NotificationChannelsEnum;
import com.bonnysimon.starter.features.role.RoleEntity;
import com.bonnysimon.starter.features.role.RoleRepository;
import com.bonnysimon.starter.features.user.UserEntity;
import com.bonnysimon.starter.features.user.UserRepository;
import com.bonnysimon.starter.features.user.dto.UserResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalLevelService {

    private final ApprovalLevelRepository repository;
    private final UserApprovalRepository userApprovalRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ApprovalActionRepository approvalActionRepository;
    private final NotificationService notificationService;
    private final ApprovalStatusUtil approvalStatusUtil;
    private final CurrentUserService currentUserService;



    @Value("${spring.front.end.url}")
    private String frontEndUrl;

    public PagedResponse<ApprovalLevelResponseDTO> findAll(
            PaginationRequest pagination,
            String search
    ) {
        Specification<ApprovalLevel> spec = getEntitySpecification(search);
        boolean hasApprovalMode = approvalStatusUtil.hasApprovalMode(ApprovalLevel.class.getSimpleName());

        Page<ApprovalLevel> page =
                repository.findAll(spec, pagination.toPageable());

        List<ApprovalLevel> entities = page.getContent();

        List<Long> ids = entities.stream()
                .map(ApprovalLevel::getId)
                .toList();
        Map<Long, String> statusMap = hasApprovalMode
                ? approvalStatusUtil.getBulkApprovalStatuses(UserEntity.class.getSimpleName(), ids)
                : Collections.emptyMap();

        List<ApprovalLevelResponseDTO> result = entities.stream()
                .map(entity -> {
                    ApprovalLevelResponseDTO dto = ApprovalLevelResponseDTO.fromEntity(entity);

                    if (hasApprovalMode) {
                        dto.setApprovalStatus(
                                statusMap.get(entity.getId())
                        );
                    }

                    return dto;
                })
                .toList();

        return new PagedResponse<>(
                result,
                new PaginationDto(
                        page.getTotalElements(),
                        page.getNumber() + 1,
                        page.getSize(),
                        page.getTotalPages()
                ),
                hasApprovalMode // or dynamic logic
        );
    }

    private static Specification< ApprovalLevel> getEntitySpecification(String search) {
        Specification< ApprovalLevel> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

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


    @Transactional
    public ApprovalLevelResponseDTO create(Long userApprovalId , ApprovalLevelRequestDTO request) {
        // 1️⃣ Validate UserApproval
        UserApproval userApproval = userApprovalRepository.findById(userApprovalId)
                .orElseThrow(() -> new IllegalStateException("User Approval Not Found"));

        // 2️⃣ Validate Role
        RoleEntity role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalStateException("Role Not Found"));

        // 3️⃣ Check existing ApprovalLevel
        repository.findByRoleIdAndUserApprovalId(request.getRoleId(), userApprovalId)
                .ifPresent(l -> {
                    throw new IllegalStateException("Approval Level already exists for this role and userApproval");
                });

        // 4️⃣ Calculate next level
        int nextLevel = updateApprovalLevelOrder(userApprovalId, "CREATE", null);

        // 5️⃣ Create new ApprovalLevel
        ApprovalLevel level = new ApprovalLevel();
        level.setName(request.getName());
        level.setDescription(request.getDescription());
        level.setLevel(nextLevel);
        level.setUserApproval(userApproval);
        level.setRole(role);

        // 6️⃣ Save new level
        ApprovalLevel saved = repository.save(level);

        // 7️⃣ Find previous level (by createdAt descending, excluding this new one)
        Optional<ApprovalLevel> previousLevelOpt =
                repository.findByUserApprovalIdAndLevel(userApproval.getId(), saved.getLevel() - 1);

        if (previousLevelOpt.isPresent()) {
            ApprovalLevel previousLevel = previousLevelOpt.get();

            // 8️⃣ Load all actions for previous level
            List<ApprovalAction> previousActions = approvalActionRepository
                    .findByApprovalLevelId(previousLevel.getId());

            // 9️⃣ Check if all are APPROVED
            boolean allApproved = previousActions.stream()
                    .allMatch(a -> a.getAction() == ApprovalActionEnum.APPROVED);

            if (allApproved) {
                // 10️⃣ Duplicate actions for new level
                List<ApprovalAction> newActions = previousActions.stream()
                        .map(a -> {
                            ApprovalAction action = new ApprovalAction();
                            action.setApprovalLevel(saved);
                            action.setUser(saved.getUser()); // or current user
                            action.setName(a.getName());
                            action.setDescription(a.getDescription());
                            action.setAction(a.getAction());
                            action.setEntityName(a.getEntityName());
                            action.setEntityId(a.getEntityId());
                            action.setType(ApprovalActionCreationTypeEnum.AUTOMATIC);
                            action.setEntityCreatorId(a.getEntityCreatorId());
                            return action;
                        })
                        .toList();

                approvalActionRepository.saveAll(newActions);
            }
        }

        try {
            sendCreateLevelNotification(saved, role);
        } catch (MessagingException e) {
            log.error("Failed to send approval level notification for level id={}", saved.getId(), e);
        }
        return ApprovalLevelResponseDTO.fromEntity(saved);
    }


    public ApprovalAwareDTO<ApprovalLevelResponseDTO> findOne  (Long  userId) {
        ApprovalLevel   entity = repository.findById( userId)
                .orElseThrow(() -> new IllegalStateException(" User not found"));

        ApprovalLevelResponseDTO dto = ApprovalLevelResponseDTO.fromEntity(entity);

        return approvalStatusUtil.attachApprovalInfo(
                dto,
                entity.getId(),
                ApprovalLevel.class.getSimpleName(),
                currentUserService.getCurrentUserRoleId()
        );
    }

    @Transactional
    public ApprovalLevel update(Long id, Long userApprovalId, ApprovalLevelRequestDTO request) {
            repository.findByRoleIdAndUserApprovalId(request.getRoleId(), userApprovalId)
                .ifPresent(l -> {
                    throw new IllegalStateException("Approval Level already exists for this role and userApproval");
                });

        ApprovalLevel level = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("ApprovalLevel not found"));

        level.setName(request.getName());
        level.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            level.setStatus(request.getStatus());
        }

        if (request.getRoleId() != null) {
            RoleEntity role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new IllegalStateException("Role not found"));
            level.setRole(role);
            level.setUser(null); // clear user if role is set
        }

        if (request.getUserId() != null) {
            UserEntity user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            level.setUser(user);
            level.setRole(null); // clear role if user is set
        }

        return repository.save(level);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        ApprovalLevel level = repository.findById(id)
                .orElseThrow(() -> new IllegalStateException("ApprovalLevel not found"));

        updateApprovalLevelOrder(level.getUserApproval().getId(), "DELETE", level);

        if (soft) {
            level.setDeleted(true);
            repository.save(level);
        } else {
            repository.delete(level);
        }
    }


    @Transactional
    public int updateApprovalLevelOrder(
            Long userApprovalId,
            String action,
            ApprovalLevel affectedLevel
    ) {

        List<ApprovalLevel> levels = repository.findByUserApprovalIdOrderByLevelAsc(userApprovalId);

        if ("CREATE".equalsIgnoreCase(action)) {
            // Return next level
            return levels.isEmpty() ? 1 : levels.get(levels.size() - 1).getLevel() + 1;
        }

        if ("DELETE".equalsIgnoreCase(action) && affectedLevel != null) {
            int deletedLevelNum = affectedLevel.getLevel();

            // Shift levels down after deletion
            for (ApprovalLevel lvl : levels) {
                if (lvl.getLevel() > deletedLevelNum) {
                    lvl.setLevel(lvl.getLevel() - 1);
                    repository.save(lvl); // update
                }
            }

            return deletedLevelNum;
        }

        return 1;
    }


    @Transactional
    public void sendCreateLevelNotification(ApprovalLevel level, RoleEntity role) throws MessagingException {

        log.info("Approval level passed level={}", toJson(level));

        String redirectUrl = frontEndUrl + "/"
                + FrontEndRouteConstants.CREATE_APPROVAL_LEVEL_REDIRECT_URL
                + "/" + level.getUserApproval().getId();

        Map<String, Object> context = new HashMap<>();
        context.put("levelName", level.getName());
        context.put("description", level.getDescription());
        context.put("status", level.getStatus());
        context.put("year", LocalDate.now().getYear());
        context.put("manageLevelLink", redirectUrl);

        List<String> recipients = new ArrayList<>();

        // 🟡 If role provided → get all users with that role
        if (role != null) {
            List<UserEntity> users = userRepository.findByRoleId(role.getId());
            recipients = users.stream()
                    .map(UserEntity::getEmail)
                    .filter(Objects::nonNull)
                    .toList();
        }

        // If no recipients → exit
        if (recipients.isEmpty()) {
            log.info("No recipients found for this level — skipping notifications.");
            return;
        }

        // Build notification DTO
        SendNotificationDto dto = new SendNotificationDto();
        dto.setChannel(NotificationChannelsEnum.EMAIL);
        dto.setRecipients(recipients);
        dto.setForName(level.getUserApproval().getSysApproval().getEntityName());
        dto.setForId(level.getId());
        dto.setContext(context);
        dto.setTemplate("create-level");
        dto.setSubject("New Level Created");
        dto.setDescription("New Level created name "
                + level.getName()
                + " for Approval "
                + level.getUserApproval().getName());
        dto.setRedirectUrl(redirectUrl);

        // Send notification
        notificationService.sendNotification(dto);
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }
}