package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.MovieDTO;
import com.cinemabooking.backend.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Movies", description = "Endpoints for movie management (Read-Only)")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping
    @Operation(summary = "Get all movies", description = "Fetches a paginated list of movies with lenient filtering for active/non-deleted items.")
    public ApiResponse<List<MovieDTO>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws ExecutionException, InterruptedException {
        List<MovieDTO> movies = movieService.getAllMovies(page, size);
        return ApiResponse.<List<MovieDTO>>builder()
                .success(true)
                .message("Movies fetched successfully")
                .data(movies)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get movie by ID")
    public ApiResponse<MovieDTO> getMovieById(@PathVariable String id) throws ExecutionException, InterruptedException {
        MovieDTO movie = movieService.getMovieById(id);
        if (movie == null) {
            return ApiResponse.<MovieDTO>builder()
                    .success(false)
                    .message("Movie not found with ID: " + id)
                    .build();
        }
        return ApiResponse.<MovieDTO>builder()
                .success(true)
                .message("Movie found")
                .data(movie)
                .build();
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get movies by status", description = "e.g., ON_SHOW, COMING_SOON")
    public ApiResponse<List<MovieDTO>> getMoviesByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws ExecutionException, InterruptedException {
        List<MovieDTO> movies = movieService.getMoviesByStatus(status, page, size);
        return ApiResponse.<List<MovieDTO>>builder()
                .success(true)
                .message("Movies with status " + status + " fetched")
                .data(movies)
                .build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search movies by title keyword")
    public ApiResponse<List<MovieDTO>> searchMovies(@RequestParam String keyword) throws ExecutionException, InterruptedException {
        List<MovieDTO> movies = movieService.searchMovies(keyword);
        return ApiResponse.<List<MovieDTO>>builder()
                .success(true)
                .message("Search results for: " + keyword)
                .data(movies)
                .build();
    }
}
