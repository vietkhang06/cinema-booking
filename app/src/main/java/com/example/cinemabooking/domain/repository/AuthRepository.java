package com.example.cinemabookingapp.domain.repository;

import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.User;

public interface AuthRepository {
    void register(String name, String email, String phone, String password, ResultCallback<User> callback);
    void login(String email, String password, ResultCallback<User> callback);
    void resetPassword(String email, ResultCallback<Void> callback);
    void logout();
    boolean isLoggedIn();
}