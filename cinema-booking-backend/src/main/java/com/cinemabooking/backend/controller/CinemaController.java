package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.CinemaDTO;
import com.cinemabooking.backend.service.CinemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/cinemas")
@Tag(name = "Cinemas", description = "Endpoints for cinema theater information (Read-Only)")
public class CinemaController {

    @Autowired
    private CinemaService cinemaService;

    @GetMapping
    @Operation(summary = "Get all cinemas")
    public ApiResponse<List<CinemaDTO>> getAllCinemas() throws ExecutionException, InterruptedException {
        List<CinemaDTO> cinemas = cinemaService.getAllCinemas();
        return ApiResponse.<List<CinemaDTO>>builder()
                .success(true)
                .message("Cinemas fetched successfully")
                .data(cinemas)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cinema by ID")
    public ApiResponse<CinemaDTO> getCinemaById(@PathVariable String id) throws ExecutionException, InterruptedException {
        CinemaDTO cinema = cinemaService.getCinemaById(id);
        if (cinema == null) {
            return ApiResponse.<CinemaDTO>builder()
                    .success(false)
                    .message("Cinema not found with ID: " + id)
                    .build();
        }
        return ApiResponse.<CinemaDTO>builder()
                .success(true)
                .message("Cinema found")
                .data(cinema)
                .build();
    }
}
