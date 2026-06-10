package com.example.cinemabooking.data.remote.api;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.domain.model.Showtime;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ShowtimeApiService {

    @GET("showtimes")
    Call<ApiResponse<List<Showtime>>> getAllShowtimes();

    @GET("showtimes/{id}")
    Call<ApiResponse<Showtime>> getShowtimeById(@Path("id") String id);

    @GET("showtimes/movie/{movieId}")
    Call<ApiResponse<List<Showtime>>> getShowtimesByMovieId(@Path("movieId") String movieId);

    @GET("showtimes/cinema/{cinemaId}")
    Call<ApiResponse<List<Showtime>>> getShowtimesByCinemaId(@Path("cinemaId") String cinemaId);

    @POST("showtimes")
    Call<ApiResponse<Showtime>> updateShowtime(@Body Showtime showtime);
}
