package com.bonnysimon.starter.features.approval;

import com.bonnysimon.starter.features.approval.dto.SysApprovalRequestDTO;
import com.bonnysimon.starter.features.approval.entity.SysApproval;
import com.bonnysimon.starter.features.approval.enums.StatusEnum;
import com.bonnysimon.starter.features.approval.repository.SysApprovalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SysApprovalSeeder implements CommandLineRunner {

    private final SysApprovalRepository repository;

    @Override
    public void run(String... args) {
        seedApprovals();
    }

    private void seedApprovals() {
        List<SysApprovalRequestDTO> approvals = List.of(
                createDto("User", "Approvals For User", "User", StatusEnum.PENDING),
                createDto("Role", "Approvals For  for Role", "Role", StatusEnum.PENDING)
        );

        for (SysApprovalRequestDTO dto : approvals) {
            if (repository.findByName(dto.getName()) == null) {
                SysApproval entity = new SysApproval();
                entity.setName(dto.getName());
                entity.setDescription(dto.getDescription());
                entity.setEntityName(dto.getEntityName());
                entity.setStatus(dto.getStatus());
                repository.save(entity);
                System.out.println("Seeded approval: " + dto.getName());
            } else {
                System.out.println("Approval already exists: " + dto.getName());
            }
        }
    }

    private SysApprovalRequestDTO createDto(String name, String description, String entityName, StatusEnum status) {
        SysApprovalRequestDTO dto = new SysApprovalRequestDTO();
        dto.setName(name);
        dto.setDescription(description);
        dto.setEntityName(entityName);
        dto.setStatus(status);
        return dto;
    }
}
