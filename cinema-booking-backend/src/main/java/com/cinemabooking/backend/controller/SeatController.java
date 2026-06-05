package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.SeatDTO;
import com.cinemabooking.backend.dto.request.SeatLockRequestDTO;
import com.cinemabooking.backend.service.SeatService;
import com.cinemabooking.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/seats")
@Tag(name = "Seats", description = "Endpoints for seat layout, locking, and releasing")
public class SeatController {

    @Autowired
    private SeatService seatService;

    @Autowired
    private UserService userService;

    @GetMapping("/showtime/{showtimeId}")
    @Operation(summary = "Get seats by Showtime ID")
    public ApiResponse<List<SeatDTO>> getSeatsByShowtimeId(@PathVariable String showtimeId) throws ExecutionException, InterruptedException {
        List<SeatDTO> seats = seatService.getSeatsByShowtimeId(showtimeId);
        return ApiResponse.<List<SeatDTO>>builder()
                .success(true)
                .message("Seats for showtime fetched successfully")
                .data(seats)
                .build();
    }

    @PostMapping("/lock")
    @Operation(summary = "Lock/hold seats for showtime")
    public ResponseEntity<ApiResponse<Void>> lockSeats(
            @AuthenticationPrincipal String userId,
            @RequestBody SeatLockRequestDTO request
    ) throws ExecutionException, InterruptedException {
        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.<Void>builder()
                                .success(false)
                                .message("Vui lòng đăng nhập.")
                                .build()
                );
            }
            seatService.lockSeats(userId, request.getShowtimeId(), request.getSeatIds());
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Seats locked successfully")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/release")
    @Operation(summary = "Release seat hold for showtime")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(
            @AuthenticationPrincipal String userId,
            @RequestBody SeatLockRequestDTO request
    ) throws ExecutionException, InterruptedException {
        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.<Void>builder()
                                .success(false)
                                .message("Vui lòng đăng nhập.")
                                .build()
                );
            }
            seatService.releaseSeats(userId, request.getShowtimeId(), request.getSeatIds());
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Seats released successfully")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/release-by-staff")
    @Operation(summary = "Release seat hold by staff/admin role")
    public ResponseEntity<ApiResponse<Void>> releaseSeatsByStaff(
            @AuthenticationPrincipal String userId,
            @RequestBody SeatLockRequestDTO request
    ) throws ExecutionException, InterruptedException {
        try {
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.<Void>builder()
                                .success(false)
                                .message("Vui lòng đăng nhập.")
                                .build()
                );
            }
            com.cinemabooking.backend.dto.UserDTO user = userService.getUserById(userId);
            if (user == null || (!"staff".equalsIgnoreCase(user.getRole()) && !"admin".equalsIgnoreCase(user.getRole()))) {
                throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
            }
            
            seatService.releaseSeatsByStaff(request.getShowtimeId(), request.getSeatIds());
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("Seats released by staff successfully")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message(e.getMessage())
                            .build()
            );
        }
    }
}

