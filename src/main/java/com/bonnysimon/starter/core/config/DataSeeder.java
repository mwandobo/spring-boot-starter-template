package com.bonnysimon.starter.core.config;

import com.bonnysimon.starter.features.permission.Permission;
import com.bonnysimon.starter.features.permission.PermissionRepository;
import com.bonnysimon.starter.features.roles.Role;
import com.bonnysimon.starter.features.roles.RoleRepository;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initData(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // 1. Default permissions
            List<String> defaultPermissions = List.of(
                    "VIEW_USERS",
                    "CREATE_USER",
                    "UPDATE_USER",
                    "DELETE_USER"
            );

            Set<Permission> permissions = new HashSet<>();
            for (String permName : defaultPermissions) {
                Permission perm = permissionRepository.findByName(permName);
                if (perm == null) {
                    perm = new Permission();
                    perm.setName(permName);
                    permissionRepository.save(perm);
                }
                permissions.add(perm);
            }

            // 2. Default role (ADMIN)
            Role adminRole = roleRepository.findByName("ADMIN");
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setName("ADMIN");
                adminRole.setPermissions(permissions);
                roleRepository.save(adminRole);
            } else {
                // Ensure permissions are synced
                adminRole.setPermissions(permissions);
                roleRepository.save(adminRole);
            }

            // 3. Default user (admin@starter.com / password: admin123)
            if (userRepository.findByEmail("admin@starter.com").isEmpty()) {
                User adminUser = new User();
                adminUser.setName("Super Admin"); // if you have username field
                adminUser.setEmail("admin@starter.com"); // if you use email
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setRole(adminRole);
                userRepository.save(adminUser);
            }
        };
    }
}
