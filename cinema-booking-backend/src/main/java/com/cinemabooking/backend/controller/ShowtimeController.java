/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend API
 * 
 * Mô tả: 
 * Controller truy vấn lịch chiếu phim. Cho phép client lọc lịch chiếu theo
 * ngày, theo rạp và theo từng bộ phim cụ thể.
 */
package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.ShowtimeDTO;
import com.cinemabooking.backend.dto.UserDTO;
import com.cinemabooking.backend.service.ShowtimeService;
import com.cinemabooking.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/showtimes")
@Tag(name = "Showtimes", description = "Endpoints for showtime schedules")
public class ShowtimeController {

    @Autowired
    private ShowtimeService showtimeService;

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Get all showtimes")
    public ApiResponse<List<ShowtimeDTO>> getAllShowtimes() throws ExecutionException, InterruptedException {
        List<ShowtimeDTO> showtimes = showtimeService.getAllShowtimes();
        return ApiResponse.<List<ShowtimeDTO>>builder()
                .success(true)
                .message("Showtimes fetched successfully")
                .data(showtimes)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get showtime by ID")
    public ApiResponse<ShowtimeDTO> getShowtimeById(@PathVariable String id) throws ExecutionException, InterruptedException {
        ShowtimeDTO showtime = showtimeService.getShowtimeById(id);
        if (showtime == null) {
            return ApiResponse.<ShowtimeDTO>builder()
                    .success(false)
                    .message("Showtime not found with ID: " + id)
                    .build();
        }
        return ApiResponse.<ShowtimeDTO>builder()
                .success(true)
                .message("Showtime found")
                .data(showtime)
                .build();
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "Get showtimes by Movie ID")
    public ApiResponse<List<ShowtimeDTO>> getShowtimesByMovieId(@PathVariable String movieId) throws ExecutionException, InterruptedException {
        List<ShowtimeDTO> showtimes = showtimeService.getShowtimesByMovieId(movieId);
        return ApiResponse.<List<ShowtimeDTO>>builder()
                .success(true)
                .message("Showtimes for movie fetched successfully")
                .data(showtimes)
                .build();
    }

    @GetMapping("/cinema/{cinemaId}")
    @Operation(summary = "Get showtimes by Cinema ID")
    public ApiResponse<List<ShowtimeDTO>> getShowtimesByCinemaId(@PathVariable String cinemaId) throws ExecutionException, InterruptedException {
        List<ShowtimeDTO> showtimes = showtimeService.getShowtimesByCinemaId(cinemaId);
        return ApiResponse.<List<ShowtimeDTO>>builder()
                .success(true)
                .message("Showtimes for cinema fetched successfully")
                .data(showtimes)
                .build();
    }

    @PostMapping("/seed")
    @Operation(summary = "Seed mock showtimes into Firestore")
    public ApiResponse<Integer> seedShowtimes(
            @AuthenticationPrincipal String userId
    ) throws ExecutionException, InterruptedException {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        UserDTO user = userService.getUserById(userId);
        if (user == null || (!"staff".equalsIgnoreCase(user.getRole()) && !"admin".equalsIgnoreCase(user.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
        }

        int count = showtimeService.seedShowtimes();
        return ApiResponse.<Integer>builder()
                .success(true)
                .message("Mock showtimes successfully seeded to Firestore")
                .data(count)
                .build();
     }

     @PostMapping
     @Operation(summary = "Update showtime details")
     public ResponseEntity<ApiResponse<ShowtimeDTO>> updateShowtime(
             @AuthenticationPrincipal String userId,
             @RequestBody ShowtimeDTO showtime
     ) throws ExecutionException, InterruptedException {
         if (userId == null) {
             throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
         }
         UserDTO user = userService.getUserById(userId);
         if (user == null || (!"staff".equalsIgnoreCase(user.getRole()) && !"admin".equalsIgnoreCase(user.getRole()))) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thực hiện thao tác này.");
         }

         if(showtime == null){
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Showtime data is required");
         }

         ShowtimeDTO updatedShowtime = showtimeService.updateShowtime(showtime);
         return ResponseEntity.ok(
                 ApiResponse.<ShowtimeDTO>builder()
                     .success(true)
                     .data(updatedShowtime)
                     .message("Showtime updated successfully")
                     .build()
         );
     }
}
