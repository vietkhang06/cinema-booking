package com.example.cinemabooking.data.remote.api;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.BookingDTO;
import com.example.cinemabooking.data.dto.SeatBookingRequestDTO;
import com.example.cinemabooking.data.dto.StaffStatsDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BookingApiService {
    @GET("bookings/my")
    Call<ApiResponse<List<BookingDTO>>> getMyBookings();

    @GET("bookings/{id}")
    Call<ApiResponse<BookingDTO>> getBookingById(@Path("id") String id);

    @GET("bookings/search")
    Call<ApiResponse<List<BookingDTO>>> searchBookings(@retrofit2.http.Query("query") String query);

    @GET("bookings/stats")
    Call<ApiResponse<StaffStatsDTO>> getStaffStats();

    @retrofit2.http.PUT("bookings/{id}/checkin")
    Call<ApiResponse<Void>> checkInBooking(@Path("id") String id);

    @retrofit2.http.POST("bookings")
    Call<ApiResponse<BookingDTO>> createBooking(@retrofit2.http.Body SeatBookingRequestDTO request);

    @retrofit2.http.PUT("bookings/payment/{id}/confirmed")
    Call<ApiResponse<Void>> confirmPayment(@retrofit2.http.Path("id") String bookingId);

    @retrofit2.http.PUT("bookings/payment/{id}/failed")
    Call<ApiResponse<Void>> cancelBooking(@retrofit2.http.Path("id") String bookingId);

    @retrofit2.http.GET("bookings/pending")
    Call<ApiResponse<BookingDTO>> getPendingBooking(@retrofit2.http.Query("showtimeId") String showtimeId);
}
