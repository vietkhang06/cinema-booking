package com.example.cinemabooking.ui.admin.user;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabooking.R;

public class AdminUserManagementActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);
        android.view.View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}