package com.bonnysimon.starter.core.config;

import com.bonnysimon.starter.features.permission.Permission;
import com.bonnysimon.starter.features.permission.PermissionRepository;
import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.role.RoleRepository;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


record PermissionDef(String name, String description) {
}

record PermissionGroup(String group, List<PermissionDef> permissions) {
}

@Configuration
public class DataSeeder {

    List<PermissionGroup> permissionGroups = List.of(

            new PermissionGroup("user", List.of(
                    new PermissionDef("user_create", "Create users"),
                    new PermissionDef("user_read", "View users"),
                    new PermissionDef("user_update", "Edit users"),
                    new PermissionDef("user_delete", "Delete users")
            )),

            new PermissionGroup("department", List.of(
                    new PermissionDef("department_create", "Create departments"),
                    new PermissionDef("department_read", "View departments"),
                    new PermissionDef("department_update", "Edit departments"),
                    new PermissionDef("department_delete", "Delete departments")
            )),

            new PermissionGroup("position", List.of(
                    new PermissionDef("position_create", "Create positions"),
                    new PermissionDef("position_read", "View positions"),
                    new PermissionDef("position_update", "Edit positions"),
                    new PermissionDef("position_delete", "Delete positions")
            )),

            new PermissionGroup("role", List.of(
                    new PermissionDef("role_create", "Create roles"),
                    new PermissionDef("role_read", "View roles"),
                    new PermissionDef("role_update", "Edit roles"),
                    new PermissionDef("role_delete", "Delete roles"),
                    new PermissionDef("role_assign", "Assign permissions to roles")
            )),
            new PermissionGroup("employee", List.of(
                    new PermissionDef("employee_create", "Create employees"),
                    new PermissionDef("employee_read", "View employees"),
                    new PermissionDef("employee_update", "Edit employees"),
                    new PermissionDef("employee_delete", "Delete employees")
            )),
            new PermissionGroup("dashboard", List.of(
                    new PermissionDef("dashboard_create", "Create dashboard"),
                    new PermissionDef("dashboard_read", "View dashboard"),
                    new PermissionDef("dashboard_update", "Edit dashboard"),
                    new PermissionDef("dashboard_delete", "Delete dashboard")
            )),
            new PermissionGroup("administration", List.of(
                    new PermissionDef("administration_read", "View administration")
            )),

            new PermissionGroup("compare_excel", List.of(
                    new PermissionDef("compare_excel_read", "Compare Excel for Reconcile")
            )),


            new PermissionGroup("report", List.of(
                    new PermissionDef("report_read", "Report Read")
            )),

            new PermissionGroup("asset_management", List.of(
                    new PermissionDef("asset_management_read", "View Asset Management")
            )),

            new PermissionGroup("asset", List.of(
                    new PermissionDef("asset_create", "Create Asset"),
                    new PermissionDef("asset_read", "View Asset"),
                    new PermissionDef("asset_update", "Edit Asset"),
                    new PermissionDef("asset_delete", "Delete Asset")
            )),

            new PermissionGroup("asset_category", List.of(
                    new PermissionDef("asset_category_create", "Create Asset Category"),
                    new PermissionDef("asset_category_read", "View Asset Category"),
                    new PermissionDef("asset_category_update", "Edit Asset Category"),
                    new PermissionDef("asset_category_delete", "Delete Asset Category")
            )),

            new PermissionGroup("asset_request", List.of(
                    new PermissionDef("asset_request_create", "Create Asset Request"),
                    new PermissionDef("asset_request_read", "View Asset Request"),
                    new PermissionDef("asset_request_update", "Edit Asset Request"),
                    new PermissionDef("asset_request_delete", "Delete Asset Request")
            )),

            new PermissionGroup("approval", List.of(
                    new PermissionDef("approval_create", "Create Approval"),
                    new PermissionDef("approval_read", "View Approval"),
                    new PermissionDef("approval_update", "Edit Approval"),
                    new PermissionDef("approval_delete", "Delete Approval")
            )),

            new PermissionGroup("dashboard", List.of(
                    new PermissionDef("dashboard_read", "View dashboards"),
                    new PermissionDef("dashboard_stats_card_read", "View dashboards Stats Cards"),
                    new PermissionDef("dashboard_activities_read", "View system Activities")
            ))


            // 👉 continue copying ALL groups from NestJS
    );


    @Bean
    @Transactional
    CommandLineRunner initData(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Set<Permission> allPermissions = new HashSet<>();

            for (PermissionGroup group : permissionGroups) {
                for (PermissionDef def : group.permissions()) {

                    Permission existing = permissionRepository.findByName(def.name());

                    if (existing == null) {
                        Permission newPerm = new Permission();
                        newPerm.setName(def.name());
                        newPerm.setDescription(def.description());
                        newPerm.setGroup(group.group());

                        existing = permissionRepository.save(newPerm);
                    }

                    allPermissions.add(existing);
                }
            }
//                    });

            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName("ADMIN");
                        role.setPermissions(new HashSet<>());
                        return roleRepository.save(role);
                    });

// Load existing permissions
            Set<Permission> existingPermissions = adminRole.getPermissions();
            if (existingPermissions == null) {
                existingPermissions = new HashSet<>();
            }

// Find missing permissions
            Set<Long> existingIds = existingPermissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());

            List<Permission> newPermissions = allPermissions.stream()
                    .filter(p -> !existingIds.contains(p.getId()))
                    .toList();

            existingPermissions.addAll(newPermissions);
            adminRole.setPermissions(existingPermissions);
            roleRepository.save(adminRole);


            // 3. Default user (admin@starter.com / password: admin123)
            if (userRepository.findByEmail("breezojr@gmail.com").isEmpty()) {
                User adminUser = new User();
                adminUser.setName("Super Admin"); // if you have username field
                adminUser.setEmail("breezojr@gmail.com"); // if you use email
                adminUser.setPhone("0764010158"); // if you use email
                adminUser.setIsOtpVerified(true); // if you use email
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setRole(adminRole);
                userRepository.save(adminUser);
            }
        };
    }
}
