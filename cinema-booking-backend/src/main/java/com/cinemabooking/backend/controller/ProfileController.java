package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.UserDTO;
import com.cinemabooking.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "Endpoints for authenticated user profile (Read-Only)")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Get current authenticated user profile")
    public ApiResponse<UserDTO> getMyProfile(@AuthenticationPrincipal Object principal) throws ExecutionException, InterruptedException {
        if (principal == null) {
            return ApiResponse.<UserDTO>builder()
                    .success(false)
                    .message("Unauthorized: No principal found in security context")
                    .build();
        }
        
        String uid = principal.toString();
        UserDTO user = userService.getUserById(uid);
        
        if (user == null) {
            return ApiResponse.<UserDTO>builder()
                    .success(false)
                    .message("User profile not found in database for UID: " + uid)
                    .build();
        }
        
        return ApiResponse.<UserDTO>builder()
                .success(true)
                .message("User profile fetched successfully")
                .data(user)
                .build();
    }
}
