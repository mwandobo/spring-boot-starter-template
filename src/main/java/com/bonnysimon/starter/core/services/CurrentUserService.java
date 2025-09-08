package com.bonnysimon.starter.core.services;

import com.bonnysimon.starter.core.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Service
public class CurrentUserService {

    @Autowired
    private JwtUtil jwtUtil;

    public Long getCurrentUserId() {
        String token = extractTokenFromRequest();
        if (token != null && jwtUtil.validateToken(token)) {
            return jwtUtil.extractUserId(token);
        }
        throw new RuntimeException("User not authenticated or invalid token");
    }

    public String getCurrentUserEmail() {
        String token = extractTokenFromRequest();
        if (token != null && jwtUtil.validateToken(token)) {
            return jwtUtil.extractEmail(token);
        }
        throw new RuntimeException("User not authenticated or invalid token");
    }

    private String extractTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                return authorizationHeader.substring(7);
            }
        }
        return null;
    }

    // Alternative method using SecurityContext
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }
}