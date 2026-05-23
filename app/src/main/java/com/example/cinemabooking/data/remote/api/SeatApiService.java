package com.example.cinemabooking.data.remote.api;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.SeatDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SeatApiService {

    @GET("seats/showtime/{showtimeId}")
    Call<ApiResponse<List<SeatDTO>>> getSeatsByShowtimeId(@Path("showtimeId") String showtimeId);
}
