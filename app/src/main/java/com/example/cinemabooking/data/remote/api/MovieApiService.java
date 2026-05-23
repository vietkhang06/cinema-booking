package com.example.cinemabooking.data.remote.api;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.domain.model.Movie;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MovieApiService {
    @GET("movies")
    Call<ApiResponse<List<Movie>>> getAllMovies(@Query("page") int page, @Query("size") int size);

    @GET("movies/{id}")
    Call<ApiResponse<Movie>> getMovieById(@Path("id") String id);

    @GET("movies/status/{status}")
    Call<ApiResponse<List<Movie>>> getMoviesByStatus(@Path("status") String status, @Query("page") int page, @Query("size") int size);

    @GET("movies/search")
    Call<ApiResponse<List<Movie>>> searchMovies(@Query("keyword") String keyword);
}
