package com.bonnysimon.starter.features.notification;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.approval.dto.ApprovalActionRequestDTO;
import com.bonnysimon.starter.features.approval.entity.ApprovalAction;
import com.bonnysimon.starter.features.approval.services.ApprovalActionService;
import com.bonnysimon.starter.features.notification.dto.CreateNotificationDto;
import com.bonnysimon.starter.features.notification.dto.NotificationResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public ApiResponse<PaginationResponse<NotificationResponseDto>> getAll(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(service.findAll(pagination, search));
    }

    @PostMapping
    public ApiResponse<NotificationEntity> create(@RequestBody CreateNotificationDto request) {
        return ApiResponse.success(service.create(request));
    }

//
//    @PutMapping("/{id}")
//    public ApiResponse<ApprovalAction> update(
//            @PathVariable Long id,
//            @RequestBody ApprovalActionRequestDTO request
//    ) {
//        return ApiResponse.success(service.update(id, request));
//    }
//
//    @DeleteMapping("/{id}")
//    public ApiResponse<Void> delete(
//            @PathVariable Long id,
//            @RequestParam(name = "soft", defaultValue = "false") boolean soft
//    ) {
//        service.delete(id, soft);
//        return ApiResponse.success(null);
//    }
}
