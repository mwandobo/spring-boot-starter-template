package com.bonnysimon.starter.features.fetch_data;

import com.bonnysimon.starter.features.administration.department.dto.DepartmentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/fetch-data")
@RequiredArgsConstructor

public class FetchDataController {

    private final FetchDataService service;

    @GetMapping("departments")
    public List<DepartmentResponseDTO> fetchDepartments(   ) {
        return service.fetchDepartments();
    }
}