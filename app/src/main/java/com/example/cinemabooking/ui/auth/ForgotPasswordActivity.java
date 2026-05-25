package com.example.cinemabooking.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.TextView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.service.AuthenticationService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends BaseActivity {

    private TextInputLayout tilEmail;
    private TextInputEditText edtEmail;
    private MaterialButton btnSendReset;
    private TextView tvBack;

    private AuthenticationService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        authService = ServiceProvider
                .getInstance(getApplicationContext())
                .getAuthenticationService();

        initViews();
        bindActions();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        edtEmail = findViewById(R.id.edtEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        tvBack = findViewById(R.id.tvBack);
    }

    private void bindActions() {
        tvBack.setOnClickListener(v -> finish());
        btnSendReset.setOnClickListener(v -> sendResetEmail());
    }

    private void sendResetEmail() {
        tilEmail.setError(null);

        String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            return;
        }

        btnSendReset.setEnabled(false);

        authService.forgetAndResetPassword(email)
                .addOnSuccessListener(unused -> {
                    btnSendReset.setEnabled(true);
                    showToast("Đã gửi email đặt lại mật khẩu");
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSendReset.setEnabled(true);
                    showToast(e.getMessage() != null ? e.getMessage() : "Gửi email thất bại");
                });
    }
}