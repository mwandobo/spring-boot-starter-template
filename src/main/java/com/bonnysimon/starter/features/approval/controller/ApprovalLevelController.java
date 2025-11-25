package com.bonnysimon.starter.features.approval.controller;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.approval.dto.ApprovalLevelRequestDTO;
import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.approval.services.ApprovalLevelService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/approval-levels")
@RequiredArgsConstructor
public class ApprovalLevelController {

    private final ApprovalLevelService service;

    @GetMapping
    public ApiResponse<PaginationResponse<ApprovalLevel>> getAll(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(service.findAll(pagination, search));
    }

    @PostMapping
    public ApiResponse<ApprovalLevel> create(@RequestBody ApprovalLevelRequestDTO request) throws MessagingException {
        return ApiResponse.success(service.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ApprovalLevel> update(
            @PathVariable Long id,
            @RequestBody ApprovalLevelRequestDTO request
    ) {
        return ApiResponse.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @RequestParam(name = "soft", defaultValue = "false") boolean soft
    ) {
        service.delete(id, soft);
        return ApiResponse.success(null);
    }
}
