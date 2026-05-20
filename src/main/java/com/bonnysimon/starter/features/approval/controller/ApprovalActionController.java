package com.bonnysimon.starter.features.approval.controller;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PagedResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.approval.dto.ApprovalActionRequestDTO;
import com.bonnysimon.starter.features.approval.dto.ApprovalActionResponseDTO;
import com.bonnysimon.starter.features.approval.dto.ApprovalAwareDTO;
import com.bonnysimon.starter.features.approval.dto.ApprovalLevelRequestDTO;
import com.bonnysimon.starter.features.approval.entity.ApprovalAction;
import com.bonnysimon.starter.features.approval.entity.ApprovalLevel;
import com.bonnysimon.starter.features.approval.services.ApprovalActionService;
import com.bonnysimon.starter.features.approval.services.ApprovalLevelService;
import com.bonnysimon.starter.features.user.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/approval-actions")
@RequiredArgsConstructor
public class ApprovalActionController {

    private final ApprovalActionService service;


    @GetMapping
    public PagedResponse<ApprovalActionResponseDTO> findAll(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        return service.findAll(pagination, search);
    }

    @PostMapping
    public ApiResponse<ApprovalAction> create(@RequestBody ApprovalActionRequestDTO request) {
        return ApiResponse.success(service.create(request));
    }

    @GetMapping("/{id}")
    public ApprovalAwareDTO<ApprovalActionResponseDTO> findOne(
            @PathVariable Long id
    ) {
        return service.findOne(id);
    }

    @PutMapping("/{id}")
    public ApiResponse<ApprovalAction> update(
            @PathVariable Long id,
            @RequestBody ApprovalActionRequestDTO request
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
