package com.example.cinemabooking.data.remote.datasource;

import android.util.Log;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Banner;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BannerRemoteDataSource {
    private static final String TAG = "BannerRemoteDataSource";
    private static final String COLLECTION = "banners";

    private final FirebaseFirestore firestore;
    private final com.example.cinemabooking.data.remote.api.BannerApiService bannerApi;

    public BannerRemoteDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
        this.bannerApi = com.example.cinemabooking.data.remote.api.RetrofitClient.getInstance().create(com.example.cinemabooking.data.remote.api.BannerApiService.class);
    }

    public void getAllBanners(ResultCallback<List<Banner>> callback) {
        Log.d(TAG, "Requesting all banners");
        bannerApi.getAllBanners().enqueue(new Callback<ApiResponse<List<Banner>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Banner>>> call, Response<ApiResponse<List<Banner>>> response) {
                Log.d(TAG, "Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Banner> data = response.body().getData();
                    Log.d(TAG, "Banners fetched: " + (data != null ? data.size() : 0));
                    if (callback != null) callback.onSuccess(data != null ? data : new ArrayList<>());
                } else {
                    String msg = (response.body() != null) ? response.body().getMessage() : "Lỗi tải banner (Code: " + response.code() + ")";
                    Log.e(TAG, "API Error: " + msg);
                    if (callback != null) {
                        callback.onSuccess(new ArrayList<>());
                        callback.onError(msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Banner>>> call, Throwable t) {
                Log.e(TAG, "Network Failure: " + t.getMessage());
                if (callback != null) {
                    callback.onSuccess(new ArrayList<>());
                    callback.onError("Không thể tải quảng cáo.");
                }
            }
        });
    }
}
