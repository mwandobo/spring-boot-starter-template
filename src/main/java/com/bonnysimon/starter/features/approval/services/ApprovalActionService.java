package com.bonnysimon.starter.features.approval.services;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.core.services.CurrentUserService;
import com.bonnysimon.starter.features.approval.dto.ApprovalActionRequestDTO;
import com.bonnysimon.starter.features.approval.entity.ApprovalAction;
import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.approval.enums.ApprovalActionEnum;
import com.bonnysimon.starter.features.approval.repository.ApprovalActionRepository;
import com.bonnysimon.starter.features.approval.repository.ApprovalLevelRepository;
import com.bonnysimon.starter.features.notification.NotificationService;
import com.bonnysimon.starter.features.notification.dto.SendNotificationDto;
import com.bonnysimon.starter.features.notification.enums.NotificationChannelsEnum;
import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalActionService {
    private final ApprovalActionRepository repository;
    private final ApprovalLevelRepository approvalLevelRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public PaginationResponse<ApprovalAction> findAll(PaginationRequest pagination, String search) {
        Specification<ApprovalAction> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%")
            );
        }

        Page<ApprovalAction> approvalLevels = repository.findAll(spec, pagination.toPageable());
        return PaginationResponse.of(approvalLevels);
    }

    @Transactional
    public ApprovalAction create(ApprovalActionRequestDTO request) {

        ApprovalLevel approvalLevel = approvalLevelRepository.findById(request.getApprovalLevelId())
                .orElseThrow(() -> new IllegalArgumentException("Approval Level not found"));

        Optional<ApprovalAction> existing =
                repository.findByApprovalLevelIdAndEntityId(
                        request.getApprovalLevelId(),
                        request.getEntityId()
                );

        if (existing.isPresent()) {
            throw new IllegalStateException( "Approval Action has been done for this level "+
                    approvalLevel.getName() + " and entity " + request.getEntityName());
        }

        if (request.getEntityCreatorId() == null) {
            throw new IllegalStateException("Entity Creator Id is missing in DTO");
        }

        Long userId = currentUserService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User Not found"));

        User entityCreator = userRepository.findById(request.getEntityCreatorId())
                .orElseThrow(() -> new IllegalStateException("User Not found"));


        ApprovalAction action = new ApprovalAction();
        action.setApprovalLevel(approvalLevel);
        action.setUser(user);
        action.setName(request.getName());
        action.setDescription(request.getDescription());
        action.setAction(request.getAction());
        action.setEntityName(request.getEntityName());
        action.setEntityId(request.getEntityId());
        action.setEntityCreatorId(request.getEntityCreatorId());

        ApprovalAction saved = repository.save(action);

        // 🔔 Send Notifications (equivalent to NestJS)
       handleApprovalNotifications(
                request, approvalLevel, entityCreator, user
        );

        return saved;

    }

    @Transactional
    public ApprovalAction update(Long id, ApprovalActionRequestDTO request) {
        ApprovalAction action = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Approval Action not found"));

        ApprovalLevel approvalLevel = approvalLevelRepository.findById(request.getApprovalLevelId())
                .orElseThrow(() -> new IllegalArgumentException("Approval Level not found"));

        Long userId = currentUserService.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User Not found"));

        action.setApprovalLevel(approvalLevel);
        action.setUser(user);
        action.setName(request.getName());
        action.setDescription(request.getDescription());
        action.setAction(request.getAction());
        action.setEntityName(request.getEntityName());
        action.setEntityId(request.getEntityId());

        return repository.save(action);
    }

    @Transactional
    public void delete(Long id, boolean soft) {
        ApprovalAction action = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ApprovalAction not found"));

        if (soft) {
            action.setDeleted(true);
            repository.save(action);
        } else {
            repository.delete(action);
        }
    }

//    @Transactional
//    public void handleApprovalNotifications(
//            ApprovalActionRequestDTO dto,
//            ApprovalLevel approvalLevel,
//            User entityCreator,
//            User performedByUser
//    ) {
//
//        // 🟥 CASE: Rejected
//        if (dto.getAction().equals(ApprovalActionEnum.REJECTED)) {
//
//            // 1️⃣ Find all previous levels
//            List<ApprovalLevel> previousLevels =
//                    approvalLevelRepository.findByUserApprovalIdAndLevelLessThanEqual(
//                            approvalLevel.getUserApproval().getId(),
//                            approvalLevel.getLevel()
//                    );
//
//            // 2️⃣ Extract role IDs
//            List<Long> roleIds = previousLevels.stream()
//                    .filter(l -> l.getRole() != null)
//                    .map(l -> l.getRole().getId())
//                    .toList();
//
//            // 3️⃣ Fetch users with those roles
//            List<User> previousApprovers = userRepository.findByRoleIdIn(roleIds);
//
//            List<String> recipients = new ArrayList<>();
//            recipients.add(entityCreator.getEmail());     // creator
//            previousApprovers.forEach(u -> recipients.add(u.getEmail()));
//
//            // Remove null emails
//            recipients.removeIf(Objects::isNull);
//
//            // 4️⃣ Build context map (like NestJS)
//            Map<String, Object> context = new HashMap<>();
//            context.put("userName", entityCreator.getName());
//            context.put("requestName", dto.getEntityName());
//            context.put("assets", dto.getExtraData1());
//            context.put("requestDescription", dto.getDescription());
//            context.put("rejectedBy", performedByUser.getName());
//            context.put("rejectionDate", LocalDate.now().toString());
//            context.put("priority", "Normal");
//            context.put("priorityColor", "red");
//            context.put("approvalLink", dto.getRedirectUrl());
//            context.put("year", Year.now().getValue());
//
//            // 5️⃣ Send notification
//            try {
//
//                SendNotificationDto notification = new SendNotificationDto();
//                notification.setChannel(NotificationChannelsEnum.EMAIL);
//                notification.setRecipients(recipients);
//                notification.setForName(approvalLevel.getUserApproval().getSysApproval().getEntityName());
//                notification.setForId(approvalLevel.getId());
//                notification.setContext(context);
//                notification.setTemplate("create-level");
//                notification.setSubject("New Level Created");
//                notification.setDescription("Approval For Entity " + dto.getEntityName() + " has been Rejected");
//                notification.setRedirectUrl(dto.getRedirectUrl());
//
//                notificationService.sendNotification(notification);
//
//                log.info("❌ Rejection email sent to creator {} and previous approvers {}",
//                        entityCreator.getEmail(), recipients);
//
//            } catch (Exception e) {
//                log.error("❌ Failed to send rejection notifications", e);
//            }
//
//            return;
//        }
//
//        // 🟦 Normal next-level logic (optional)
//        log.info("Checking for next approval level after level {} ({})...",
//                approvalLevel.getId(), approvalLevel.getName());
//    }


    @Transactional
    public void handleApprovalNotifications(
            ApprovalActionRequestDTO dto,
            ApprovalLevel approvalLevel,
            User entityCreator,
            User performedByUser
    ) {

        // 🟥 CASE 1: Request Rejected
        if (dto.getAction().equals(ApprovalActionEnum.REJECTED)) {

            List<ApprovalLevel> previousLevels =
                    approvalLevelRepository.findByUserApprovalIdAndLevelLessThanEqual(
                            approvalLevel.getUserApproval().getId(),
                            approvalLevel.getLevel()
                    );

            List<Long> roleIds = previousLevels.stream()
                    .filter(l -> l.getRole() != null)
                    .map(l -> l.getRole().getId())
                    .toList();

            List<User> previousApprovers = userRepository.findByRoleIdIn(roleIds);

            List<String> recipients = new ArrayList<>();
            recipients.add(entityCreator.getEmail());
            recipients.addAll(
                    previousApprovers.stream()
                            .map(User::getEmail)
                            .filter(Objects::nonNull)
                            .toList()
            );

            Map<String, Object> context = new HashMap<>();
            context.put("userName", entityCreator.getName());
            context.put("requestName", dto.getEntityName());
            context.put("assets", dto.getExtraData1());
            context.put("requestDescription", dto.getDescription());
            context.put("rejectedBy", performedByUser.getName());
            context.put("rejectionDate", LocalDate.now().toString());
            context.put("priority", "Normal");
            context.put("priorityColor", "red");
            context.put("approvalLink", dto.getRedirectUrl());
            context.put("year", Year.now().getValue());

            try {
                SendNotificationDto notification = new SendNotificationDto();
                notification.setChannel(NotificationChannelsEnum.EMAIL);
                notification.setRecipients(recipients);
                notification.setForName(approvalLevel.getUserApproval().getSysApproval().getEntityName());
                notification.setForId(approvalLevel.getId());
                notification.setContext(context);
                notification.setTemplate("create-level");
                notification.setSubject("New Level Created");
                notification.setDescription(
                        "Approval For Entity " + dto.getEntityName() + " has been Rejected"
                );
                notification.setRedirectUrl(dto.getRedirectUrl());

                notificationService.sendNotification(notification);

                log.info("❌ Rejection email sent to: {}", recipients);

            } catch (Exception e) {
                log.error("❌ Failed to send rejection notifications", e);
            }

            return;
        }

        // 🟦 CASE 2: Check if there's a next level
        log.info("Checking for next approval level after {} ({})…",
                approvalLevel.getId(), approvalLevel.getName());

        Optional<ApprovalLevel> nextLevel = approvalLevelRepository
                .findFirstByUserApprovalIdAndLevelGreaterThanOrderByLevelAsc(
                        approvalLevel.getUserApproval().getId(),
                        approvalLevel.getLevel()
                );

        // -----------------------------------------------
        // 🟩 FINAL LEVEL → Send final approval email
        // -----------------------------------------------
        if (nextLevel.isEmpty()) {

            log.info("checking extradata {}", dto.getExtraData1().toString());


            Map<String, Object> context = new HashMap<>();
            context.put("userName", entityCreator.getName());
            context.put("requestId", dto.getEntityId());
            context.put("requestName", dto.getEntityName());
            context.put("assets", dto.getExtraData1());
            context.put("requestDescription", dto.getDescription());
            context.put("finalLevelName", approvalLevel.getName());
            context.put("approvedBy", performedByUser.getName());
            context.put("approvalDate", LocalDate.now().toString());
            context.put("priority", "Normal");
            context.put("priorityColor", "blue");
            context.put("approvalLink", dto.getRedirectUrl());
            context.put("year", Year.now().getValue());

            try {
                SendNotificationDto notification = new SendNotificationDto();
                notification.setChannel(NotificationChannelsEnum.EMAIL);
                notification.setRecipients(List.of(entityCreator.getEmail()));
                notification.setForName(approvalLevel.getUserApproval().getSysApproval().getEntityName());
                notification.setForId(approvalLevel.getId());
                notification.setContext(context);
                notification.setTemplate("request-approved");
                notification.setSubject("Approval Complete For Entity: " + dto.getEntityName());
                notification.setDescription(
                        "Approval request for the entity " + dto.getEntityName() + " has been completed"
                );
                notification.setRedirectUrl(dto.getRedirectUrl());

                notificationService.sendNotification(notification);

                log.info("✅ Final approval email sent to {}", entityCreator.getEmail());

            } catch (Exception e) {
                log.error("❌ Failed to send final approval email", e);
            }

            return;
        }

        ApprovalLevel _nextLevel = nextLevel.get();

        // -----------------------------------------------
        // 🟦 CASE 3: There *is* a next level → Notify next approvers
        // -----------------------------------------------
        log.info("Next level found: {} (role: {})",
                _nextLevel.getName(),
                _nextLevel.getRole() != null ? _nextLevel.getRole().getName() : "N/A");

        Role role = _nextLevel.getRole();

        List<String> recipients = new ArrayList<>();

        if (role != null) {
            List<User> users = userRepository.findByRoleId(role.getId());
            recipients = users.stream()
                    .map(User::getEmail)
                    .filter(Objects::nonNull)
                    .toList();

            log.info("Users for role {} → {}", role.getName(), recipients);
        }

        if (recipients.isEmpty()) {
            log.warn("No recipients found for next approval level {}", _nextLevel.getName());
            return;
        }

        Map<String, Object> context = new HashMap<>();
        context.put("userName", performedByUser.getName());
        context.put("requestId", dto.getEntityId());
        context.put("requestName", dto.getEntityName());
        context.put("assets", dto.getExtraData1());
        context.put("requestDescription", dto.getDescription());
        context.put("currentLevelName", approvalLevel.getName());
        context.put("nextLevelName", _nextLevel.getName());
        context.put("submittedBy", performedByUser.getEmail());
        context.put("submissionDate", LocalDate.now().toString());
        context.put("priority", "Normal");
        context.put("priorityColor", "blue");
        context.put("dueDate", "Not specified");
        context.put("year", Year.now().getValue());
        context.put("approvalLink", dto.getRedirectUrl());

        try {
            SendNotificationDto notification = new SendNotificationDto();
            notification.setChannel(NotificationChannelsEnum.EMAIL);
            notification.setRecipients(recipients);
            notification.setForName(approvalLevel.getUserApproval().getSysApproval().getEntityName());
            notification.setForId(approvalLevel.getId());
            notification.setContext(context);
            notification.setTemplate("next-approval");
            notification.setSubject("Approval Required: " + dto.getEntityName());
            notification.setDescription("Approval Required for " + dto.getEntityName());
            notification.setRedirectUrl(dto.getRedirectUrl());

            notificationService.sendNotification(notification);

            log.info("📨 Next-level notification sent to {}", recipients);

        } catch (Exception e) {
            log.error("❌ Failed to send next-level notification", e);
        }
    }

}
