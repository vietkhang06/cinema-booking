package com.example.cinemabooking.data.remote.datasource;

import android.util.Log;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CinemaRemoteDataSource {
    private static final String TAG = "CinemaRemoteDataSource";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final com.example.cinemabooking.data.remote.api.CinemaApiService cinemaApi;
    private static final String COLLECTION = "cinemas";

    public CinemaRemoteDataSource() {
        this.cinemaApi = com.example.cinemabooking.data.remote.api.RetrofitClient.getInstance().create(com.example.cinemabooking.data.remote.api.CinemaApiService.class);
    }

    // =========================
    // GET ALL
    // =========================
    public void getAllCinemas(ResultCallback<List<Cinema>> callback) {
        Log.d(TAG, "Requesting all cinemas");
        cinemaApi.getAllCinemas().enqueue(new Callback<ApiResponse<List<Cinema>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Cinema>>> call, Response<ApiResponse<List<Cinema>>> response) {
                Log.d(TAG, "Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Cinema> data = response.body().getData();
                    Log.d(TAG, "Cinemas fetched: " + (data != null ? data.size() : 0));
                    if (callback != null) callback.onSuccess(data != null ? data : new ArrayList<>());
                } else {
                    String msg = (response.body() != null) ? response.body().getMessage() : "Lỗi tải rạp (Code: " + response.code() + ")";
                    Log.e(TAG, "API Error: " + msg);
                    if (callback != null) {
                        callback.onSuccess(new ArrayList<>());
                        callback.onError(msg);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Cinema>>> call, Throwable t) {
                Log.e(TAG, "Network Failure: " + t.getMessage());
                if (callback != null) {
                    callback.onSuccess(new ArrayList<>());
                    callback.onError("Không thể tải danh sách rạp phim.");
                }
            }
        });
    }

    // =========================
    // CREATE (AUTO ID hoặc CUSTOM ID)
    // =========================
    public void createCinema(Cinema cinema, ResultCallback<Cinema> callback) {

        // 👉 OPTION 1: AUTO ID (recommended)
        DocumentReference docRef = db.collection(COLLECTION).document();

        cinema.cinemaId = docRef.getId();

        // 👉 OPTION 2: CUSTOM ID (nếu muốn dạng c001)
        // String customId = "c" + System.currentTimeMillis();
        // cinema.cinemaId = customId;
        // DocumentReference docRef = db.collection(COLLECTION).document(customId);

        long now = System.currentTimeMillis();

        cinema.createdAt = now;
        cinema.updatedAt = now;
        cinema.deleted = false;

        docRef.set(cinema)
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess(cinema);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    // =========================
    // GET BY ID
    // =========================
    public void getCinemaById(String id, ResultCallback<Cinema> callback) {
        Log.d(TAG, "Requesting cinema by ID: " + id);
        cinemaApi.getCinemaById(id).enqueue(new Callback<ApiResponse<Cinema>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cinema>> call, Response<ApiResponse<Cinema>> response) {
                Log.d(TAG, "Response Code: " + response.code());
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Cinema fetched: " + response.body().getData().name);
                    if (callback != null) callback.onSuccess(response.body().getData());
                } else {
                    String msg = (response.body() != null) ? response.body().getMessage() : "Không tìm thấy rạp (Code: " + response.code() + ")";
                    Log.e(TAG, "API Error: " + msg);
                    if (callback != null) callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Cinema>> call, Throwable t) {
                Log.e(TAG, "Network Failure: " + t.getMessage());
                if (callback != null) callback.onError("Lỗi kết nối khi tải thông tin rạp.");
            }
        });
    }

    // =========================
    // UPDATE
    // =========================
    public void updateCinema(Cinema cinema, ResultCallback<Cinema> callback) {

        if (cinema.cinemaId == null) {
            callback.onError("CinemaId null");
            return;
        }

        cinema.updatedAt = System.currentTimeMillis();

        db.collection(COLLECTION)
                .document(cinema.cinemaId)
                .set(cinema)
                .addOnSuccessListener(unused -> callback.onSuccess(cinema))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // =========================
    // SOFT DELETE
    // =========================
    public void softDeleteCinema(String id, ResultCallback<Void> callback) {

        db.collection(COLLECTION)
                .document(id)
                .update("deleted", true,
                        "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // =========================
    // MAPPER
    // =========================
    private Cinema mapDocToCinema(DocumentSnapshot doc) {

        Cinema c = new Cinema();

        c.cinemaId = doc.getId();
        c.name = doc.getString("name");
        c.address = doc.getString("address");
        c.city = doc.getString("city");
        c.district = doc.getString("district");
        c.phone = doc.getString("phone");
        c.status = doc.getString("status");

        c.latitude = getDouble(doc.get("latitude"));
        c.longitude = getDouble(doc.get("longitude"));

        c.createdAt = getLong(doc.get("createdAt"));
        c.updatedAt = getLong(doc.get("updatedAt"));

        Boolean deleted = doc.getBoolean("deleted");
        c.deleted = deleted != null && deleted;

        return c;
    }

    // =========================
    // TYPE SAFE
    // =========================
    private long getLong(Object o) {
        try {
            if (o instanceof Long) return (Long) o;
            if (o instanceof String) return Long.parseLong((String) o);
        } catch (Exception ignored) {}
        return 0;
    }

    private double getDouble(Object o) {
        try {
            if (o instanceof Double) return (Double) o;
            if (o instanceof Long) return ((Long) o).doubleValue();
            if (o instanceof String) return Double.parseDouble((String) o);
        } catch (Exception ignored) {}
        return 0;
    }
}