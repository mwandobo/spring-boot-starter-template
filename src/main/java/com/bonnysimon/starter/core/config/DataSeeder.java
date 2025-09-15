package com.bonnysimon.starter.core.config;

import com.bonnysimon.starter.features.permission.Permission;
import com.bonnysimon.starter.features.permission.PermissionRepository;
import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.role.RoleRepository;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.model.UserOtp;
import com.bonnysimon.starter.features.user.repository.UserOtpRepository;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initData(
            PermissionRepository permissionRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserOtpRepository userOtpRepository
    ) {
        return args -> {
            // 1. Default permissions
            List<String> defaultPermissions = List.of(
                    "VIEW_USERS",
                    "CREATE_USER",
                    "UPDATE_USER",
                    "DELETE_USER",
                    "VIEW_ROLE"
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

            // 2. Admin role
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setName("ADMIN");
                        newRole.setPermissions(new HashSet<>()); // initialize empty set
                        return roleRepository.save(newRole);
                    });

            // Append only missing permissions
            Set<Permission> rolePermissions = adminRole.getPermissions();
            boolean updated = false;
            for (Permission perm : permissions) {
                if (!rolePermissions.contains(perm)) {
                    rolePermissions.add(perm);
                    updated = true;
                }
            }
            if (updated) {
                adminRole.setPermissions(rolePermissions);
                roleRepository.save(adminRole);
            }

            // 3. Default admin user
            if (userRepository.findByEmail("admin@starter.com").isEmpty()) {
                User adminUser = new User();
                adminUser.setName("Super Admin");
                adminUser.setEmail("admin@starter.com");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setRole(adminRole);
                userRepository.save(adminUser);

                String otp = generateOtp();
                UserOtp userOtp = new UserOtp();
                userOtp.setUser(adminUser);
                userOtp.setOtp(otp);
                userOtp.setVerified(true);
                userOtp.setExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));
                userOtpRepository.save(userOtp);
            }
        };
    }

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000)); // 6-digit OTP
    }
}
