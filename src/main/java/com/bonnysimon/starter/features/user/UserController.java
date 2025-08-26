package com.bonnysimon.starter.features.user;
import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.user.model.User;
import org.springframework.web.bind.annotation.*;


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