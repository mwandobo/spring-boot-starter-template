package com.example.super_start_web_api.features.auth;

import com.example.super_start_web_api.core.utils.JwtUtil;
import com.example.super_start_web_api.features.auth.dtos.LoginRequest;
import com.example.super_start_web_api.features.auth.dtos.LoginResponse;
import com.example.super_start_web_api.features.auth.dtos.RegisterRequest;
import com.example.super_start_web_api.features.user.model.User;
import com.example.super_start_web_api.features.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

            return new LoginResponse(jwt);
        } catch (Exception ex) {
            logger.error("Login failed for user: {}", loginRequest.getEmail(), ex);
            throw ex; // or throw a custom AuthenticationException if you handle that way
        }
    }




    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setName(registerRequest.getName());
        userRepository.save(user);
    }
}