package com.example.cinemabooking.ui.admin.log;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabooking.R;
import com.example.cinemabooking.ui.admin.AdminBottomNavHelper;

public class AdminAuditLogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_audit_log);

        AdminBottomNavHelper.setupAdminBottomNavigation(this, 3);

        android.view.View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }
}