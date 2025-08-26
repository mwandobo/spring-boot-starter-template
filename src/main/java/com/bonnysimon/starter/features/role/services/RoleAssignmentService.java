package com.bonnysimon.starter.features.role.services;

import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.role.RoleRepository;
import com.bonnysimon.starter.features.role.dto.AssignRoleRequest;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleAssignmentService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public User assignRolesToUser(AssignRoleRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(role);

        return userRepository.save(user);
    }
}