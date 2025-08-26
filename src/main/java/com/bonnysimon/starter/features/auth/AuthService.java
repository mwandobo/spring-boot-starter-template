package com.bonnysimon.starter.features.auth;

import com.bonnysimon.starter.core.utils.JwtUtil;
import com.bonnysimon.starter.features.auth.dtos.LoginRequest;
import com.bonnysimon.starter.features.auth.dtos.LoginResponse;
import com.bonnysimon.starter.features.auth.dtos.RegisterRequest;
import com.bonnysimon.starter.features.permission.Permission;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final Logger logger = LoggerFactory.getLogger(AuthService.class);


    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }


//    public LoginResponse login(LoginRequest loginRequest) {
//        try {
//            logger.info("Attempting login for user: {}", loginRequest.getEmail());
//
//
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            loginRequest.getEmail(),
//                            loginRequest.getPassword()
//                    )
//            );
//
//            logger.info("Authentication successful for user: {}", loginRequest.getEmail());
//
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//
//            String jwt = jwtUtil.generateToken(loginRequest.getEmail());
//            logger.info("JWT generated for user: {}", loginRequest.getEmail());
//
//            return new LoginResponse(jwt, );
//        } catch (Exception ex) {
//            logger.error("Login failed for user: {}", loginRequest.getEmail(), ex);
//            throw ex; // or throw a custom AuthenticationException if you handle that way
//        }
//    }


    public LoginResponse login(LoginRequest loginRequest) {
        try {
            logger.info("Attempting login for user: {}", loginRequest.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            logger.info("Authentication successful for user: {}", loginRequest.getEmail());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtil.generateToken(loginRequest.getEmail());
            logger.info("JWT generated for user: {}", loginRequest.getEmail());

            // Get user from DB
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            String roleName = user.getRole() != null ? user.getRole().getName() : null;
            Set<String> permissions = user.getRole() != null
                    ? user.getRole().getPermissions().stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet())
                    : Set.of();

            return new LoginResponse(jwt, user.getEmail(), roleName, permissions);

        } catch (Exception ex) {
            logger.error("Login failed for user: {}", loginRequest.getEmail(), ex);
            throw ex; // or throw a custom AuthenticationException
        }
    }


    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalStateException("Email is already taken!");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setName(registerRequest.getName());
        userRepository.save(user);
    }
}