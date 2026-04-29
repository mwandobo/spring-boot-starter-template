package com.bonnysimon.starter.features.approval.util;

import com.bonnysimon.starter.features.approval.dto.ApprovalAwareDTO;
import com.bonnysimon.starter.features.approval.entity.ApprovalAction;
import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.approval.entity.SysApproval;
import com.bonnysimon.starter.features.approval.entity.UserApproval;
import com.bonnysimon.starter.features.approval.enums.ApprovalActionEnum;
import com.bonnysimon.starter.features.approval.repository.ApprovalActionRepository;
import com.bonnysimon.starter.features.approval.repository.ApprovalLevelRepository;
import com.bonnysimon.starter.features.approval.repository.SysApprovalRepository;
import com.bonnysimon.starter.features.approval.repository.UserApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalStatusUtil {

    private final SysApprovalRepository sysApprovalRepository;
    private final UserApprovalRepository userApprovalRepository;
    private final ApprovalLevelRepository approvalLevelRepository;
    private final ApprovalActionRepository approvalActionRepository;

    /**
     * Check if approval mode is enabled
     */

    public boolean hasApprovalMode(String entityName) {
        log.debug("Checking approval mode for entity: {}", entityName);

        Optional<SysApproval> sys = sysApprovalRepository.findByEntityName(entityName);
        if (sys.isEmpty()) {
            log.debug("No SysApproval found for entity: {}", entityName);
            return false;
        }

        log.debug("Found SysApproval with id: {}", sys.get().getId());

        Optional<UserApproval> userApproval =
                userApprovalRepository.findBySysApprovalId(sys.get().getId());

        if (userApproval.isEmpty()) {
            log.debug("No UserApproval found for sysApprovalId: {}", sys.get().getId());
            return false;
        }

        log.debug("Found UserApproval with id: {}", userApproval.get().getId());

        List<ApprovalLevel> levels =
                approvalLevelRepository.findByUserApprovalId(userApproval.get().getId());

        if (levels.isEmpty()) {
            log.debug("No ApprovalLevels found for userApprovalId: {}", userApproval.get().getId());
            return false;
        }

        log.debug("Approval mode ACTIVE for entity: {} with {} levels",
                entityName, levels.size());

        return true;
    }

    /**
     * Determine approval status
     */
    public String getApprovalStatus(String entityName, Long entityId) {

        UserApproval userApproval = getUserApproval(entityName);
        if (userApproval == null) return "PENDING";

        List<ApprovalLevel> levels =
                approvalLevelRepository.findByUserApprovalId(userApproval.getId());

        if (levels.isEmpty()) return "PENDING";

        List<Long> levelIds = levels.stream()
                .map(ApprovalLevel::getId)
                .collect(Collectors.toList());

        List<ApprovalAction> actions =
                approvalActionRepository.findByEntityIdAndApprovalLevelIdIn(entityId, levelIds);

        if (actions.isEmpty()) return "PENDING";

        // Check rejection
        if (actions.stream().anyMatch(a -> a.getAction() == ApprovalActionEnum.REJECTED)) {
            return "REJECTED";
        }

        for (ApprovalLevel level : levels) {

            List<ApprovalAction> levelActions = actions.stream()
                    .filter(a -> a.getApprovalLevel().getId().equals(level.getId()))
                    .toList();

            log.debug("Level {} → {} actions: {}",
                    level.getId(),
                    levelActions.size(),
                    levelActions.stream()
                            .map(a -> a.getAction().name())
                            .collect(Collectors.joining(", "))
            );

            if (levelActions.isEmpty()) return "PENDING";

            boolean approved = levelActions.stream()
                    .anyMatch(a -> a.getAction() == ApprovalActionEnum.APPROVED);

            if (!approved) return "PENDING";
        }

        return "APPROVED";
    }

    /**
     * Bulk status
     */

    public Map<Long, String> getBulkApprovalStatuses(String entityName, List<Long> entityIds) {

        log.debug("==== BULK APPROVAL STATUS START ====");
        log.debug("Entity: {}", entityName);
        log.debug("Entity IDs: {}", entityIds);

        Map<Long, String> statuses = new HashMap<>();

        UserApproval userApproval = getUserApproval(entityName);
        if (userApproval == null) {
            log.debug("No UserApproval found → defaulting all to PENDING");
            entityIds.forEach(id -> statuses.put(id, "PENDING"));
            return statuses;
        }

        log.debug("UserApproval ID: {}", userApproval.getId());

        List<ApprovalLevel> levels =
                approvalLevelRepository.findByUserApprovalId(userApproval.getId());

        log.debug("Approval Levels count: {}", levels.size());
        log.debug("Approval Levels: {}", levels.stream()
                .map(ApprovalLevel::getId)
                .toList());

        if (levels.isEmpty()) {
            log.debug("No levels found → defaulting all to PENDING");
            entityIds.forEach(id -> statuses.put(id, "PENDING"));
            return statuses;
        }

        List<ApprovalAction> actions =
                approvalActionRepository.findByEntityNameAndEntityIdIn(entityName, entityIds);

        log.debug("Total actions fetched: {}", actions.size());

        actions.forEach(a -> log.debug(
                "Action → entityId: {}, levelId: {}, action: {}",
                a.getEntityId(),
                a.getApprovalLevel().getId(),
                a.getAction()
        ));

        // Group by entityId
        Map<Long, List<ApprovalAction>> actionsByEntity =
                actions.stream().collect(Collectors.groupingBy(ApprovalAction::getEntityId));

        for (Long entityId : entityIds) {

            log.debug("---- Evaluating entityId: {} ----", entityId);

            List<ApprovalAction> entityActions =
                    actionsByEntity.getOrDefault(entityId, new ArrayList<>());

            log.debug("Total actions for entity {}: {}", entityId, entityActions.size());

            if (entityActions.isEmpty()) {
                log.debug("No actions found → PENDING");
                statuses.put(entityId, "PENDING");
                continue;
            }

            boolean rejected = false;
            boolean pending = false;

            for (ApprovalLevel level : levels) {

                List<ApprovalAction> levelActions = entityActions.stream()
                        .filter(a -> a.getApprovalLevel().getId().equals(level.getId()))
                        .toList();

                log.debug("Level {} → {} actions", level.getId(), levelActions.size());

                levelActions.forEach(a -> log.debug(
                        "   ↳ action: {}",
                        a.getAction()
                ));

                // 🚨 REJECTION CHECK
                if (levelActions.stream()
                        .anyMatch(a -> a.getAction() == ApprovalActionEnum.REJECTED)) {

                    log.debug("Entity {} REJECTED at level {}", entityId, level.getId());
                    rejected = true;
                    break;
                }

                // 🚨 APPROVAL CHECK
                boolean approved = levelActions.stream()
                        .anyMatch(a -> a.getAction() == ApprovalActionEnum.APPROVED);

                log.debug("Level {} approved? {}", level.getId(), approved);

                if (!approved) {
                    log.debug("Entity {} is still PENDING at level {}", entityId, level.getId());
                    pending = true;
                }
            }

            String finalStatus;
            if (rejected) finalStatus = "REJECTED";
            else if (pending) finalStatus = "PENDING";
            else finalStatus = "APPROVED";

            log.debug("Final status for entity {} → {}", entityId, finalStatus);

            statuses.put(entityId, finalStatus);
        }

        log.debug("==== BULK APPROVAL STATUS END ====");
        return statuses;
    }

    /**
     * Helpers
     */
    private UserApproval getUserApproval(String entityName) {
        Optional<SysApproval> sys = sysApprovalRepository.findByEntityName(entityName);
        if (sys.isEmpty()) return null;

        return userApprovalRepository
                .findBySysApprovalId(sys.get().getId())
                .orElse(null);
    }


    public <T> ApprovalAwareDTO<T> attachApprovalInfo(
            T entity,
            Long entityId,
            String entityName,
            Long userRoleId
    ) {

        log.debug("==== ATTACH APPROVAL INFO START ====");
        log.debug("Entity: {}, ID: {}, UserRoleId: {}", entityName, entityId, userRoleId);

        boolean hasApprovalMode = hasApprovalMode(entityName);
        String approvalStatus = getApprovalStatus(entityName, entityId);

        log.debug("hasApprovalMode: {}, approvalStatus: {}", hasApprovalMode, approvalStatus);

        if (!hasApprovalMode || "REJECTED".equals(approvalStatus)) {
            log.debug("Skipping approval logic (mode off or rejected)");
            return buildBasic(entity, hasApprovalMode, approvalStatus);
        }

        UserApproval userApproval = getUserApproval(entityName);

        if (userApproval == null) {
            log.debug("No UserApproval found → fallback");
            return buildBasic(entity, hasApprovalMode, approvalStatus);
        }

        log.debug("UserApproval ID: {}", userApproval.getId());

        List<ApprovalLevel> levels = getLevelsByUserApproval(userApproval.getId());

        log.debug("Levels count: {}", levels.size());
        log.debug("Levels: {}", levels.stream()
                .map(l -> "ID=" + l.getId() + ", role=" + l.getRole().getId())
                .toList());

        List<Long> levelIds = levels.stream()
                .map(ApprovalLevel::getId)
                .toList();

        List<ApprovalAction> actions = getActions(entityId, levelIds);

        log.debug("Total actions fetched: {}", actions.size());
        actions.forEach(a -> log.debug(
                "Action → levelId: {}, action: {}",
                a.getApprovalLevel().getId(),
                a.getAction()
        ));

        boolean isMyLevelApproved = false;
        boolean shouldApprove = false;

        ApprovalLevel myLevel = levels.stream()
                .filter(level -> level.getRole().getId().equals(userRoleId))
                .findFirst()
                .orElse(null);

        if (myLevel == null) {
            log.debug("No matching level found for userRoleId: {}", userRoleId);
        } else {
            log.debug("My level found → ID: {}, createdAt: {}",
                    myLevel.getId(), myLevel.getCreatedAt());

            List<ApprovalAction> myActions = actions.stream()
                    .filter(a -> a.getApprovalLevel().getId().equals(myLevel.getId()))
                    .toList();

            log.debug("My level actions count: {}", myActions.size());

            isMyLevelApproved = myActions.stream()
                    .anyMatch(a -> a.getAction() == ApprovalActionEnum.APPROVED);

            log.debug("isMyLevelApproved: {}", isMyLevelApproved);
        }

        if ("PENDING".equals(approvalStatus) && myLevel != null && !isMyLevelApproved) {

            List<ApprovalLevel> previousLevels = levels.stream()
                    .filter(lvl ->
                            lvl.getCreatedAt() != null &&
                                    myLevel.getCreatedAt() != null &&
                                    lvl.getCreatedAt().isBefore(myLevel.getCreatedAt())
                    )
                    .toList();

            log.debug("Previous levels: {}", previousLevels.stream()
                    .map(ApprovalLevel::getId)
                    .toList());

            boolean allPrevApproved = previousLevels.stream().allMatch(lvl -> {
                boolean approved = actions.stream().anyMatch(a ->
                        a.getApprovalLevel().getId().equals(lvl.getId()) &&
                                a.getAction() == ApprovalActionEnum.APPROVED
                );

                log.debug("Level {} approved? {}", lvl.getId(), approved);
                return approved;
            });

            shouldApprove = allPrevApproved;

            log.debug("shouldApprove: {}", shouldApprove);
        }

        log.debug("==== ATTACH APPROVAL INFO END ====");

        return new ApprovalAwareDTO<>(
                entity,
                hasApprovalMode,
                approvalStatus,
                isMyLevelApproved,
                shouldApprove,
                myLevel != null ? myLevel.getId() : null
        );
    }


    private <T> ApprovalAwareDTO<T> buildDefault(T entity) {
        return new ApprovalAwareDTO<>(entity, false, "N/A", false, false, null);
    }

    private <T> ApprovalAwareDTO<T> buildBasic(T entity, boolean hasApprovalMode, String status) {
        return new ApprovalAwareDTO<>(entity, hasApprovalMode, status, false, false, null);
    }






    public List<ApprovalLevel> getLevelsByUserApproval(Long userApprovalId) {
        return approvalLevelRepository.findByUserApprovalId(userApprovalId);
    }

    public List<ApprovalAction> getActions(Long entityId, List<Long> levelIds) {
        return approvalActionRepository
                .findByEntityIdAndApprovalLevelIdIn(entityId, levelIds);
    }
}
