package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.ShowtimeDTO;
import com.cinemabooking.backend.service.ShowtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/showtimes")
@Tag(name = "Showtimes", description = "Endpoints for showtime schedules")
public class ShowtimeController {

    @Autowired
    private ShowtimeService showtimeService;

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
    public ApiResponse<Integer> seedShowtimes() throws ExecutionException, InterruptedException {
        int count = showtimeService.seedShowtimes();
        return ApiResponse.<Integer>builder()
                .success(true)
                .message("Mock showtimes successfully seeded to Firestore")
                .data(count)
                .build();
    }
}
