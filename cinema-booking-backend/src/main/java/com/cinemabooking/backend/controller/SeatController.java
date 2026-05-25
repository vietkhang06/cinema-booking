package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.SeatDTO;
import com.cinemabooking.backend.service.SeatService;
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
@RequestMapping("/api/v1/seats")
@Tag(name = "Seats", description = "Endpoints for seat layout and status (Read-Only)")
public class SeatController {

    @Autowired
    private SeatService seatService;

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
}
