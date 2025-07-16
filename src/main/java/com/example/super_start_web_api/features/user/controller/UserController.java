package com.example.super_start_web_api.features.user.controller;
import com.example.super_start_web_api.features.user.model.User;
import com.example.super_start_web_api.features.user.service.UserService;
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
    public List<User> getUsers() {
        return service.getAllUsers();
    }
}