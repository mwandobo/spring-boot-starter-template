package com.bonnysimon.starter.features.role;

import com.bonnysimon.starter.features.role.dto.AssignRoleRequest;
import com.bonnysimon.starter.features.role.services.RoleAssignmentService;
import com.bonnysimon.starter.features.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleAssignmentService roleAssignmentService;

    @PostMapping("/assign")
    public User assignRoles(@RequestBody AssignRoleRequest request) {
        return roleAssignmentService.assignRolesToUser(request);
    }
}