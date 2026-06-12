package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.BannerDTO;
import com.cinemabooking.backend.dto.UserDTO;
import com.cinemabooking.backend.service.BannerService;
import com.cinemabooking.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/banners")
@Tag(name = "Banners", description = "Endpoints for advertising banners (Read-Only)")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Get all banners")
    public ApiResponse<List<BannerDTO>> getAllBanners() throws ExecutionException, InterruptedException {
        List<BannerDTO> banners = bannerService.getAllBanners();
        return ApiResponse.<List<BannerDTO>>builder()
                .success(true)
                .message("Banners fetched successfully")
                .data(banners)
                .build();
    }

    @PostMapping("/seed")
    @Operation(summary = "Seed mock banners into Firestore")
    public ApiResponse<String> seedBanners(
            @AuthenticationPrincipal String userId
    ) throws ExecutionException, InterruptedException {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        UserDTO user = userService.getUserById(userId);
        if (user == null || (!"staff".equalsIgnoreCase(user.getRole()) && !"admin".equalsIgnoreCase(user.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
        }

        bannerService.seedMockBanners();
        return ApiResponse.<String>builder()
                .success(true)
                .message("Mock banners successfully seeded to Firestore")
                .data("OK")
                .build();
    }
}