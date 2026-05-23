package com.example.cinemabooking.data.remote.api;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.domain.model.User;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ProfileApiService {
    @GET("profile")
    Call<ApiResponse<User>> getMyProfile();
}
