package com.bonnysimon.starter.features.permission;

import com.bonnysimon.starter.features.permission.dto.AssignPermissionRequest;
import com.bonnysimon.starter.features.permission.services.PermissionAssignmentService;
import com.bonnysimon.starter.features.role.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionAssignmentService permissionAssignmentService;

    @PostMapping("/assign")
    public Role assignPermissions(@RequestBody AssignPermissionRequest request) {
        return permissionAssignmentService.assignPermissionsToRole(request);
    }
}
