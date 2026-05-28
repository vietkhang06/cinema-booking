package com.example.cinemabooking.data.remote.api;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.AuditLogDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuditLogApiService {
    @POST("audit-logs")
    Call<ApiResponse<Void>> createAuditLog(@Body AuditLogDTO logDTO);
}
