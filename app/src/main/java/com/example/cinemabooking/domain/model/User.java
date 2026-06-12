package com.example.cinemabooking.domain.model;

public class User {
    public String uid;
    public String name;
    public String email;
    public String phone;
    public String avatarUrl;
    public String role;
    public String status;
    public String memberLevel;
    public Integer points;
    public String fcmToken;
    public Long createdAt;
    public Long updatedAt;
    public Boolean deleted;
    public String birthDate;
    public String gender;

    public User() {
    }

    public String getEmail() {
        return this.email;
    }

    public String getRole() {
        return this.role;
    }
}