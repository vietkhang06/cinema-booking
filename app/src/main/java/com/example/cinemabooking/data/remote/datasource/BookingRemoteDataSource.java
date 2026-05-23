package com.example.cinemabooking.data.remote.datasource;

import android.util.Log;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.BookingDTO;
import com.example.cinemabooking.data.mapper.BookingMapper;
import com.example.cinemabooking.data.remote.api.BookingApiService;
import com.example.cinemabooking.data.remote.api.RetrofitClient;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Booking;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingRemoteDataSource {
    private static final String TAG = "BookingRemoteDataSource";
    private final BookingApiService bookingApi;

    public BookingRemoteDataSource() {
        this.bookingApi = RetrofitClient.getInstance().create(BookingApiService.class);
    }

    public void getMyBookings(ResultCallback<List<Booking>> callback) {
        Log.d(TAG, "Requesting my bookings");
        bookingApi.getMyBookings().enqueue(new Callback<ApiResponse<List<BookingDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingDTO>>> call, Response<ApiResponse<List<BookingDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<BookingDTO> dtos = response.body().getData();
                    List<Booking> domainModels = dtos != null ? 
                            dtos.stream().map(BookingMapper::toDomain).collect(Collectors.toList()) : 
                            new ArrayList<>();
                    
                    Log.d(TAG, "Fetched " + domainModels.size() + " bookings");
                    if (callback != null) callback.onSuccess(domainModels);
                } else {
                    String error = (response.body() != null) ? response.body().getMessage() : "Lỗi hệ thống (" + response.code() + ")";
                    Log.e(TAG, "Error fetching bookings: " + error);
                    if (callback != null) callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookingDTO>>> call, Throwable t) {
                Log.e(TAG, "Network failure: " + t.getMessage(), t);
                if (callback != null) callback.onError("Không thể kết nối máy chủ. Vui lòng thử lại.");
            }
        });
    }

    public void getBookingById(String id, ResultCallback<Booking> callback) {
        Log.d(TAG, "Requesting booking details for ID: " + id);
        bookingApi.getBookingById(id).enqueue(new Callback<ApiResponse<BookingDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingDTO>> call, Response<ApiResponse<BookingDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Booking booking = BookingMapper.toDomain(response.body().getData());
                    Log.d(TAG, "Fetched booking detail successfully");
                    if (callback != null) callback.onSuccess(booking);
                } else {
                    String error = (response.body() != null) ? response.body().getMessage() : "Không tìm thấy thông tin vé (" + response.code() + ")";
                    Log.e(TAG, "Error fetching booking detail: " + error);
                    if (callback != null) callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingDTO>> call, Throwable t) {
                Log.e(TAG, "Network failure: " + t.getMessage(), t);
                if (callback != null) callback.onError("Lỗi mạng. Không thể tải thông tin vé.");
            }
        });
    }
}
