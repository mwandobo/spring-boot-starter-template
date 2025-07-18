package com.example.super_start_web_api.features.user.service;

import com.example.super_start_web_api.core.dto.PaginationRequest;
import com.example.super_start_web_api.core.dto.PaginationResponse;
import com.example.super_start_web_api.features.user.model.User;
import com.example.super_start_web_api.features.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
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

    public PaginationResponse<User> findAll(PaginationRequest pagination, String search) {
        Specification<User> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        Page<User> users = repo.findAll(spec, pagination.toPageable());

        Page<User> userDtos = users;

        return PaginationResponse.of(userDtos);
    }
}
