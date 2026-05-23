package com.example.cinemabooking.data.remote.api;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.BookingDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BookingApiService {
    @GET("bookings/my")
    Call<ApiResponse<List<BookingDTO>>> getMyBookings();

    @GET("bookings/{id}")
    Call<ApiResponse<BookingDTO>> getBookingById(@Path("id") String id);
}
