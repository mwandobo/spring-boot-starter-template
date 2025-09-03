package com.bonnysimon.starter.features.approval.controller;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.approval.dto.UserApprovalRequestDTO;
import com.bonnysimon.starter.features.approval.entity.UserApproval;
import com.bonnysimon.starter.features.approval.services.UserApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-approvals")
@RequiredArgsConstructor
public class UserApprovalController {

    private final UserApprovalService service;

    // ✅ Get all UserApprovals (with search + pagination)
    @GetMapping
    public ApiResponse<PaginationResponse<UserApproval>> getAll(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(
                service.findAll(pagination, search)
        );
    }

    // ✅ Create new UserApproval
    @PostMapping
    public ApiResponse<UserApproval> create(@RequestBody UserApprovalRequestDTO request) {
        return ApiResponse.success(service.create(request));
    }

    // ✅ Update UserApproval
    @PutMapping("/{id}")
    public ApiResponse<UserApproval> update(
            @PathVariable Long id,
            @RequestBody UserApprovalRequestDTO request
    ) {
        return ApiResponse.success(service.update(id, request));
    }

    // ✅ Delete UserApproval (soft or hard)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @RequestParam(name = "soft", defaultValue = "false") boolean soft
    ) {
        service.delete(id, soft);
        return ApiResponse.success(null);
    }
}
