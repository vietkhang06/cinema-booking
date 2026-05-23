package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "System", description = "System health and version information")
public class SystemController {

    @GetMapping("/health")
    @Operation(summary = "Check backend health status")
    public ApiResponse<String> health() {
        return ApiResponse.<String>builder()
                .success(true)
                .message("Backend is running")
                .data("UP")
                .build();
    }

    @GetMapping("/version")
    @Operation(summary = "Get current API version")
    public ApiResponse<String> version() {
        return ApiResponse.<String>builder()
                .success(true)
                .message("Current API version fetched")
                .data("v1.0.27-hybrid")
                .build();
    }
}
