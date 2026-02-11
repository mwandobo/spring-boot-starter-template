package com.bonnysimon.starter.features.department;

import com.bonnysimon.starter.core.dto.ApiResponse;
import com.bonnysimon.starter.core.dto.PaginationRequest;
import com.bonnysimon.starter.core.dto.PaginationResponse;
import com.bonnysimon.starter.features.department.dto.CreateDepartmentDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService service;

    @GetMapping
    public ApiResponse<PaginationResponse<DepartmentEntity>> findAll(
            PaginationRequest pagination,
            @RequestParam(required = false) String search
    ) {
        return ApiResponse.success(
                service.findAll(pagination, search)
        );
    }

    @PostMapping
    public ApiResponse<DepartmentEntity> create(
            @RequestBody CreateDepartmentDTO request
    ) {
        return ApiResponse.success(
                service.create(request)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<DepartmentEntity> update(
            @PathVariable Long id,
            @RequestBody CreateDepartmentDTO request
    ) {
        return ApiResponse.success(
                service.update(id, request)
        );
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
