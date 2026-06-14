package com.example.cinemabooking.data.repository;

import android.util.Log;

import com.example.cinemabooking.core.constants.FirestoreCollections;
import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.User;
import com.example.cinemabooking.domain.repository.UserRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepositoryImpl implements UserRepository {
    private static final String TAG = "UserRepositoryImpl";

    FirebaseFirestore firestore;
    private final com.example.cinemabooking.data.remote.api.ProfileApiService profileApi;

    public UserRepositoryImpl(){
        this.firestore = FirebaseFirestore.getInstance();
        this.profileApi = com.example.cinemabooking.data.remote.api.RetrofitClient.getInstance().create(com.example.cinemabooking.data.remote.api.ProfileApiService.class);
    }
    @Override
    public void createUser(User user, ResultCallback<User> callback) {
        firestore.collection(FirestoreCollections.USERS).document(user.uid)
                .set(user)
                .addOnSuccessListener(doc -> {
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e->{
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void getUserById(String uid, ResultCallback<User> callback) {
        Log.d(TAG, "Requesting profile directly from Firestore for UID: " + uid);
        firestore.collection(FirestoreCollections.USERS).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.uid = documentSnapshot.getId();
                            Log.d(TAG, "Profile fetched successfully from Firestore for UID: " + uid + ", points: " + user.points);
                            if (callback != null) callback.onSuccess(user);
                        } else {
                            Log.w(TAG, "Failed to parse user document from Firestore, falling back to API");
                            fetchProfileFromApi(uid, callback);
                        }
                    } else {
                        Log.d(TAG, "User document does not exist in Firestore, falling back to API");
                        fetchProfileFromApi(uid, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to fetch profile from Firestore, falling back to API: " + e.getMessage());
                    fetchProfileFromApi(uid, callback);
                });
    }

    private void fetchProfileFromApi(String uid, ResultCallback<User> callback) {
        Log.d(TAG, "Requesting profile from API for UID: " + uid);
        profileApi.getMyProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                int code = response.code();
                Log.d(TAG, "Response Code: " + code);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "Profile fetched successfully from API for UID: " + uid);
                        if (callback != null) callback.onSuccess(apiResponse.getData());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "API success but no data/message";
                        Log.e(TAG, "API Business Error: " + errorMsg);
                        if (callback != null) callback.onError(errorMsg);
                    }
                } else {
                    // Handle non-successful response (4xx, 5xx)
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    
                    String msg = "Lỗi hệ thống (Code: " + code + ")";
                    if (code == 401 || code == 403) {
                        msg = "Phiên đăng nhập hết hạn hoặc không có quyền truy cập.";
                    } else if (code >= 500) {
                        msg = "Máy chủ đang gặp sự cố. Vui lòng thử lại sau. (Code: " + code + ")";
                    }
                    
                    Log.e(TAG, "API Request Failed. Code: " + code + ", Error: " + errorBody);
                    if (callback != null) callback.onError(msg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e(TAG, "Network Failure: " + t.getMessage());
                if (callback != null) callback.onError("Không thể kết nối đến máy chủ. Vui lòng kiểm tra internet.");
            }
        });
    }


    @Override
    public void getAllUsers(ResultCallback<List<User>> callback) {
        firestore.collection(FirestoreCollections.USERS)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.uid = doc.getId();
                            users.add(user);
                        }
                    }
                    if (callback != null) callback.onSuccess(users);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void updateUser(User user, ResultCallback<User> callback) {
        firestore.collection(FirestoreCollections.USERS)
                .document(user.uid)
                .set(user, SetOptions.mergeFields("phone", "avatarUrl", "name", "birthDate", "gender"))
                .addOnSuccessListener((a) -> {
                    callback.onSuccess(user);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    @Override
    public void updateRole(String uid, String role, ResultCallback<User> callback) {

    }

    @Override
    public void updateStatus(String uid, String status, ResultCallback<User> callback) {

    }

    @Override
    public void softDeleteUser(String uid, ResultCallback<Void> callback) {

    }
}
