package com.example.super_start_web_api.features.user.controller;
import com.example.super_start_web_api.core.dto.ApiResponse;
import com.example.super_start_web_api.core.dto.PaginationRequest;
import com.example.super_start_web_api.core.dto.PaginationResponse;
import com.example.super_start_web_api.features.user.model.User;
import com.example.super_start_web_api.features.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }


    @GetMapping
    public ApiResponse<PaginationResponse<User>> getAllUsers(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(
                service.findAll(pagination, search)
        );
    }
}