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
        Log.d(TAG, "Requesting profile for UID: " + uid);
        firestore.collection(FirestoreCollections.USERS).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            Log.d(TAG, "Profile fetched successfully for UID: " + uid);
                            if (callback != null) callback.onSuccess(user);
                        } else {
                            if (callback != null) callback.onError("Không thể chuyển đổi dữ liệu User");
                        }
                    } else {
                        if (callback != null) callback.onError("Không tìm thấy User");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user: " + e.getMessage());
                    if (callback != null) callback.onError(e.getMessage());
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
