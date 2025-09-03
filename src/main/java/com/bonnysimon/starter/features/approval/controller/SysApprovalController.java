package com.bonnysimon.starter.features.approval.controller;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.approval.entity.SysApproval;
import com.bonnysimon.starter.features.approval.services.SysApprovalService;
import com.bonnysimon.starter.features.permission.Permission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sys-approvals")
@RequiredArgsConstructor
public class SysApprovalController {

    private final SysApprovalService service;

    @GetMapping()
    public ApiResponse<PaginationResponse<SysApproval>> getAllPermissions(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(
                service.findAll(pagination, search)
        );
    }
}
