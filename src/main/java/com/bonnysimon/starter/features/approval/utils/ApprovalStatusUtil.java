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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ApprovalStatusUtil {
    private final SysApprovalRepository sysApprovalRepository;
    private final UserApprovalRepository userApprovalRepository;
    private final ApprovalLevelRepository approvalLevelRepository;
    private final ApprovalActionRepository approvalActionRepository;

    /**
     * Check if an entity has approval mode enabled
     * (meaning: it has a user approval and at least one approval level).
     */
    public boolean hasApprovalMode(String entityName) {
        Optional<SysApproval> sysApprovalOpt = sysApprovalRepository
                .findByEntityName(entityName);

        System.out.println(sysApprovalOpt);


        if (sysApprovalOpt.isEmpty()) {
            return false;
        }

        SysApproval sysApproval = sysApprovalOpt.get();


        Optional<UserApproval> userApprovalOpt = userApprovalRepository
                .findBySysApprovalId(sysApproval.getId());  // 🔹 find by systemApprovalId

        if (userApprovalOpt.isEmpty()) {
            return false;
        }

        List<ApprovalLevel> levels = approvalLevelRepository.findByUserApproval(userApprovalOpt.get());
        return !levels.isEmpty();
    }

    /**
     * Determine approval status for a given entity:
     * APPROVED if all levels approved,
     * REJECTED if any action is disapprove,
     * PENDING otherwise.
     */
    public String getApprovalStatus(String entityName, String entityId) {
        List<ApprovalAction> actions = approvalActionRepository
                .findByEntityNameAndEntityId(entityName, entityId);

        if (actions.isEmpty()) {
            return "PENDING";
        }

        boolean rejected = actions.stream()
                .anyMatch(a -> a.getAction() == ApprovalActionEnum.REJECTED);

        if (rejected) {
            return "REJECTED";
        }

        // Check if all levels have an APPROVE action
        boolean allApproved = actions.stream()
                .allMatch(a -> a.getAction() == ApprovalActionEnum.APPROVED);

        return allApproved ? "APPROVED" : "PENDING";
    }
}
