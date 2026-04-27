package com.bonnysimon.starter.features.approval.util;

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
//    public boolean hasApprovalMode(String entityName) {
//        Optional<SysApproval> sys = sysApprovalRepository.findByEntityName(entityName);
//        if (sys.isEmpty()) return false;
//
//        Optional<UserApproval> userApproval =
//                userApprovalRepository.findBySysApprovalId(sys.get().getId());
//        if (userApproval.isEmpty()) return false;
//
//        List<ApprovalLevel> levels =
//                approvalLevelRepository.findByUserApprovalId(userApproval.get().getId());
//
//        return !levels.isEmpty();
//    }

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

        Map<Long, String> statuses = new HashMap<>();

        UserApproval userApproval = getUserApproval(entityName);
        if (userApproval == null) {
            entityIds.forEach(id -> statuses.put(id, "PENDING"));
            return statuses;
        }

        List<ApprovalLevel> levels =
                approvalLevelRepository.findByUserApprovalId(userApproval.getId());

        if (levels.isEmpty()) {
            entityIds.forEach(id -> statuses.put(id, "PENDING"));
            return statuses;
        }

        List<ApprovalAction> actions =
                approvalActionRepository.findByEntityNameAndEntityIdIn(entityName, entityIds);

        // Group by entityId
        Map<Long, List<ApprovalAction>> actionsByEntity =
                actions.stream().collect(Collectors.groupingBy(ApprovalAction::getEntityId));

        for (Long entityId : entityIds) {

            List<ApprovalAction> entityActions =
                    actionsByEntity.getOrDefault(entityId, new ArrayList<>());

            if (entityActions.isEmpty()) {
                statuses.put(entityId, "PENDING");
                continue;
            }

            boolean rejected = false;
            boolean pending = false;

            for (ApprovalLevel level : levels) {

                List<ApprovalAction> levelActions = entityActions.stream()
                        .filter(a -> a.getApprovalLevel().getId().equals(level.getId()))
                        .toList();

                if (levelActions.stream()
                        .anyMatch(a -> a.getAction() == ApprovalActionEnum.REJECTED)) {
                    rejected = true;
                    break;
                }

                boolean approved = levelActions.stream()
                        .anyMatch(a -> a.getAction() == ApprovalActionEnum.APPROVED);

                if (!approved) pending = true;
            }

            if (rejected) statuses.put(entityId, "REJECTED");
            else if (pending) statuses.put(entityId, "PENDING");
            else statuses.put(entityId, "APPROVED");
        }

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

    public List<ApprovalLevel> getLevelsByUserApproval(Long userApprovalId) {
        return approvalLevelRepository.findByUserApprovalId(userApprovalId);
    }

    public List<ApprovalAction> getActions(Long entityId, List<Long> levelIds) {
        return approvalActionRepository
                .findByEntityIdAndApprovalLevelIdIn(entityId, levelIds);
    }
}
