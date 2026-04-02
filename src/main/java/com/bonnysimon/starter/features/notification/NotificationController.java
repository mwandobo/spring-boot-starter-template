package com.bonnysimon.starter.features.notification;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PagedResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.features.notification.dto.CreateNotificationDto;
import com.bonnysimon.starter.features.notification.dto.NotificationResponseDto;
import com.bonnysimon.starter.features.notification.dto.SendNotificationDto;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public PagedResponse<NotificationResponseDto> getAll(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        return service.findAll(pagination, search);
    }

    @PostMapping
    public ApiResponse<NotificationEntity> create(@RequestBody CreateNotificationDto request) {
        return ApiResponse.success(service.create(request));
    }

    @PostMapping("/send-notifications")
    public ApiResponse<String> sendNotifaction(@RequestBody SendNotificationDto request) throws MessagingException {
        return ApiResponse.success(service.sendNotification(request));
    }

    @GetMapping("/{id}")
    public NotificationResponseDto findOne(
            @PathVariable Long id
    ) {
        return service.findOne(id);
    }

    @GetMapping("/{id}/read")
    public NotificationResponseDto read(
            @PathVariable Long id
    ) {
        return service.read(id);
    }


    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
            @PathVariable Long id,
            @RequestParam(name = "soft", defaultValue = "false") boolean soft
    ) {
        service.delete(id, soft);
        return ApiResponse.success(null);
    }
}