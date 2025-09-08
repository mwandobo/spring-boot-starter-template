package com.bonnysimon.starter.features.user;
import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.approval.utils.ApprovalStatusUtil;
import com.bonnysimon.starter.features.user.model.User;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {
    private final UserService service;
    private final ApprovalStatusUtil approvalStatusUtil;

//    public UserController(UserService service, ApprovalStatusUtil approvalStatusUtil) {
//        this.service = service;
//        this.approvalStatusUtil = approvalStatusUtil;
//    }


    @GetMapping
    public ApiResponse<PaginationResponse<User>> getAllUsers(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        PaginationResponse<User> users = service.findAll(pagination, search);
        boolean hasApprovalMode = approvalStatusUtil.hasApprovalMode("User");
        return ApiResponse.success(users, hasApprovalMode);
    }

}