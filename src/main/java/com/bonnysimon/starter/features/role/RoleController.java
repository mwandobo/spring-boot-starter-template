package com.bonnysimon.starter.features.role;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PagedResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.features.role.dto.AssignPermissionsRequestDTO;
import com.bonnysimon.starter.features.role.dto.AssignRoleRequest;
import com.bonnysimon.starter.features.role.dto.CreateRoleRequest;
import com.bonnysimon.starter.features.role.dto.RoleResponseDTO;
import com.bonnysimon.starter.features.role.dto.RoleWithPermissionsDTO;
import com.bonnysimon.starter.features.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService service;

    @GetMapping
    public PagedResponse<RoleResponseDTO> getAllUsers(
            PaginationRequest pagination,
            @RequestParam(required = false) String search) {
        return service.findAll(pagination, search);
    }

    @PostMapping()
    public RoleEntity create(@RequestBody CreateRoleRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public RoleResponseDTO getOne(
            @PathVariable Long id) {
        return service.findOne(id);
    }

    // ✅ Update role
    @PutMapping("/{id}")
    public ApiResponse<RoleEntity> update(@PathVariable Long id, @RequestBody CreateRoleRequest request) {
        return ApiResponse.success(service.update(id, request));
    }

    // @PostMapping("/assign")
    // public UserEntity assignRoles(@RequestBody AssignRoleRequest request) {
    // return service.assignRolesToUser(request);
    // }

    @PostMapping("/assign/{id}")
    public RoleResponseDTO assignPermissions(
        @PathVariable Long id,
        @RequestBody AssignPermissionsRequestDTO request) {
        return service.assignPermissions(id, request);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @RequestParam(name = "soft", defaultValue = "false") boolean soft) {
        service.delete(id, soft);
        return ApiResponse.success(null);
    }

    @GetMapping("permissions/{id}")
    public RoleWithPermissionsDTO getRoleWithPermissions(
            @PathVariable Long id) {
        return service.getRoleWithPermissions(id);
    }
}