package com.bonnysimon.starter.features.fetch_data;

import com.bonnysimon.starter.features.administration.department.dto.DepartmentResponseDTO;
import com.bonnysimon.starter.features.approval.dto.SysApprovalResponseDTO;
import com.bonnysimon.starter.features.asset_management.assetcategory.dto.AssetCategoryResponseDTO;
import com.bonnysimon.starter.features.role.dto.RoleResponseDTO;
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

    @GetMapping("roles")
    public List<RoleResponseDTO> fetchRoles(   ) {
        return service.fetchRoles();
    }

    @GetMapping("sys-approvals")
    public List<SysApprovalResponseDTO> fetchSysApprovals(   ) {
        return service.fetchSysApprovals();
    }

    @GetMapping("asset-categories")
    public List<AssetCategoryResponseDTO> fetchAssetCategories(   ) {
        return service.fetchAssetCategories();
    }
}