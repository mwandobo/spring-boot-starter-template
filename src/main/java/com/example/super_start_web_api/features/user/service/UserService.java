package com.example.super_start_web_api.features.user.service;

import com.example.super_start_web_api.features.user.model.User;
import com.example.super_start_web_api.features.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }
}
