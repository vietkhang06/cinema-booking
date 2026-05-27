package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.User;

public interface AuthRepository {
    void register(String name, String email, String phone, String password, ResultCallback<User> callback);
    void login(String email, String password, ResultCallback<User> callback);
    void resetPassword(String email, ResultCallback<Void> callback);
    void logout();
    boolean isLoggedIn();
}