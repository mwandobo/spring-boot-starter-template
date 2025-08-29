package com.bonnysimon.starter.features.role;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.role.dto.AssignRoleRequest;
import com.bonnysimon.starter.features.role.dto.CreateRoleRequest;
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

    @PostMapping()
    public Role create(@RequestBody CreateRoleRequest request) {
        return service.create(request);
    }

    // ✅ Update role
    @PutMapping("/{id}")
    public ApiResponse<Role> update(@PathVariable Long id, @RequestBody CreateRoleRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    @PostMapping("/assign")
    public User assignRoles(@RequestBody AssignRoleRequest request) {
        return service.assignRolesToUser(request);
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