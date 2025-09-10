package com.bonnysimon.starter.features.user;

import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.core.utils.RandomGenerator;
import com.bonnysimon.starter.features.approval.entity.ApprovalAction;
import com.bonnysimon.starter.features.auth.AuthService;
import com.bonnysimon.starter.features.role.Role;
import com.bonnysimon.starter.features.role.RoleRepository;
import com.bonnysimon.starter.features.user.dto.CreateUserDTO;
import com.bonnysimon.starter.features.user.dto.UserResponse;
import com.bonnysimon.starter.features.user.model.User;
import com.bonnysimon.starter.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final RandomGenerator randomGenerator;
    private final RoleRepository roleRepository;


//    public PaginationResponse<UserResponse> findAll(PaginationRequest pagination, String search) {
//        Specification<User> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));
//
//        if (search != null && !search.trim().isEmpty()) {
//            spec = spec.and((root, query, cb) ->
//                    cb.or(
//                            cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
//                            cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%")
//                    )
//            );
//        }
//
//        Page<User> users = repository.findAll(spec, pagination.toPageable());
//
//
//
//        return PaginationResponse.of(users);
//    }

    public PaginationResponse<UserResponse> findAll(PaginationRequest pagination, String search) {
        Specification<User> spec = (root, query, cb) -> cb.isFalse(root.get("deleted"));

        if (search != null && !search.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("email")), "%" + search.toLowerCase() + "%")
                    )
            );
        }

        Page<User> users = repository.findAll(spec, pagination.toPageable());

        Page<UserResponse> userResponses = users.map(UserResponse::fromEntity);

        return PaginationResponse.of(userResponses);
    }


    @Transactional
    public UserResponse create(CreateUserDTO dto) {
        if (repository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Email is already taken!");
        }

        String password = randomGenerator.generateRandomPassword(8);

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(password));
        user.setName(dto.getName());

        if(dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role Not found"));
            user.setRole(role);
        }

       User savedUser = repository.save(user);

        return new UserResponse( savedUser,password);
    }

    public UserResponse findOne(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        return new UserResponse(user, null);
    }

    @Transactional
    public UserResponse update(Long id, CreateUserDTO dto) {

       User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        user.setName(dto.getName());

        if(dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Role Not found"));
            user.setRole(role);
        }

        User savedUser = repository.save(user);

        return new UserResponse(savedUser, null);
    }

    public void delete(Long id, boolean soft) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        if (soft) {
            user.setDeleted(true); // soft delete flag from BaseEntity
            repository.save(user);
        } else {
            repository.delete(user);
        }
    }


}
