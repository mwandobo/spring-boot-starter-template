package com.bonnysimon.starter.features.permission;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.permission.dto.AssignPermissionRequest;
import com.bonnysimon.starter.features.permission.services.PermissionService;
import com.bonnysimon.starter.features.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService service;

    @GetMapping()
    public ApiResponse<PaginationResponse<Permission>> getAllPermissions(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(
                service.findAll(pagination, search)
        );
    }

    @PostMapping("/assign")
    public Role assignPermissions(@RequestBody AssignPermissionRequest request) {
        return service.assignPermissionsToRole(request);
    }
}
