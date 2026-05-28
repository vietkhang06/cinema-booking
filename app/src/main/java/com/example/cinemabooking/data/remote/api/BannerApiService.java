package com.example.cinemabooking.data.remote.api;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.domain.model.Banner;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface BannerApiService {
    @GET("banners")
    Call<ApiResponse<List<Banner>>> getAllBanners();
}
