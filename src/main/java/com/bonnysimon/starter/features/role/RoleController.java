package com.bonnysimon.starter.features.role;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.role.dto.AssignRoleRequest;
import com.bonnysimon.starter.features.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService service;

    @GetMapping
    public ApiResponse<PaginationResponse<Role>> getAllUsers(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(
                service.findAll(pagination, search)
        );
    }

    @PostMapping("/assign")
    public User assignRoles(@RequestBody AssignRoleRequest request) {
        return service.assignRolesToUser(request);
    }
}