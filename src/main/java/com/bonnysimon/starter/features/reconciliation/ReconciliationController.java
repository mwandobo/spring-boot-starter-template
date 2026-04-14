package com.bonnysimon.starter.features.reconciliation;

import com.bonnysimon.starter.features.reconciliation.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/excel")
@RequiredArgsConstructor
public class ReconciliationController {

    private final ReconciliationService service;

    @PostMapping("/compare")
    public Map<String, Object> compare(@RequestParam("files") MultipartFile[] files) {
        return service.compareExcel(files);
    }
}