package com.example.cinemabooking.data.remote.api;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.CineShopOrderRequestDTO;
import com.example.cinemabooking.data.dto.CineShopOrderResponseDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CineShopApiService {
    @POST("cine-shop/orders")
    Call<ApiResponse<CineShopOrderResponseDTO>> createOrder(@Body CineShopOrderRequestDTO request);
}
