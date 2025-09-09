package com.bonnysimon.starter.features.approval.utils;

import com.bonnysimon.starter.features.approval.entity.ApprovalAction;
import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.approval.entity.SysApproval;
import com.bonnysimon.starter.features.approval.entity.UserApproval;
import com.bonnysimon.starter.features.approval.enums.ApprovalActionEnum;
import com.bonnysimon.starter.features.approval.repository.ApprovalActionRepository;
import com.bonnysimon.starter.features.approval.repository.SysApprovalRepository;
import com.bonnysimon.starter.features.approval.repository.UserApprovalRepository;
import com.bonnysimon.starter.features.approval.repository.ApprovalLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApprovalStatusUtil {

    private final SysApprovalRepository sysApprovalRepository;
    private final UserApprovalRepository userApprovalRepository;
    private final ApprovalLevelRepository approvalLevelRepository;
    private final ApprovalActionRepository approvalActionRepository;

    /**
     * Check if an entity has approval mode enabled
     * (it has a SysApproval, UserApproval, and at least one level).
     */
    public boolean hasApprovalMode(String entityName) {
        return sysApprovalRepository.findByEntityName(entityName)
                .flatMap(sys -> userApprovalRepository.findBySysApprovalId(sys.getId()))
                .map(userApproval -> !approvalLevelRepository.findByUserApproval(userApproval).isEmpty())
                .orElse(false);
    }

    /**
     * Determine approval status for a given entity.
     * Rules:
     * - REJECTED: if any level has a REJECTED action.
     * - PENDING: if any level is missing approval or not yet acted on.
     * - APPROVED: only if all levels are approved.
     */
    public String getApprovalStatus(String entityName, String entityId) {
        UserApproval userApproval = getUserApproval(entityName)
                .orElse(null);

        if (userApproval == null) {
            return "PENDING";
        }

        List<ApprovalLevel> levels = approvalLevelRepository.findByUserApproval(userApproval);
        if (levels.isEmpty()) {
            return "PENDING";
        }

        List<ApprovalAction> actions = approvalActionRepository.findByEntityNameAndEntityId(entityName, entityId);
        if (actions.isEmpty()) {
            return "PENDING";
        }

        for (ApprovalLevel level : levels) {
            List<ApprovalAction> levelActions = actions.stream()
                    .filter(a -> a.getApprovalLevel().getId().equals(level.getId()))
                    .toList();

            if (levelActions.isEmpty()) {
                return "PENDING"; // no action taken on this level yet
            }
            if (levelActions.stream().anyMatch(a -> a.getAction() == ApprovalActionEnum.REJECTED)) {
                return "REJECTED"; // rejection takes priority
            }
            if (levelActions.stream().noneMatch(a -> a.getAction() == ApprovalActionEnum.APPROVED)) {
                return "PENDING"; // has actions but not approved yet
            }
        }

        return "APPROVED"; // all levels approved
    }

    private Optional<UserApproval> getUserApproval(String entityName) {
        return sysApprovalRepository.findByEntityName(entityName)
                .flatMap(sys -> userApprovalRepository.findBySysApprovalId(sys.getId()));
    }
}
