package com.example.cinemabookingapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.config.auth.FacebookAuthProviderConfig;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.AuthCallback;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.service.AuthenticationService;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// ✅ Google Sign-In
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class RegisterActivity extends BaseActivity {

    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword, tilPhone;
    private TextInputEditText edtEmail, edtPassword, edtConfirmPassword, edtPhone;

    private MaterialButton btnRegister;
    private TextView tvBack;

    private MaterialCardView btnFacebook, btnGoogle;

    private AuthenticationService authService;

    // Facebook
    private CallbackManager callbackManager;

    // Google
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = ServiceProvider.getInstance().getAuthenticationService();

        initViews();
        initGoogle();
        initFacebook();
        bindActions();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilPhone = findViewById(R.id.tilPhone);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtPhone = findViewById(R.id.edtPhone);

        btnRegister = findViewById(R.id.btnRegister);
        tvBack = findViewById(R.id.tvBack);

        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
    }

    private void initGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initFacebook() {
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult result) {
                authService.handleFacebookAccessToken(result.getAccessToken(), new AuthCallback() {
                    @Override
                    public void onSuccess(User data) {
                        AppNavigator.goToCustomerHome(RegisterActivity.this);
                    }

                    @Override
                    public void onError(String message) {
                        showToast("Facebook login failed");
                    }
                });
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException error) {
                Log.e("FacebookAuth", error.getMessage());
            }
        });
    }

    private void bindActions() {
        tvBack.setOnClickListener(v -> AppNavigator.goToLogin(this));

        btnRegister.setOnClickListener(v -> attemptRegister());

        btnFacebook.setOnClickListener(v -> signInWithFacebook());

        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void attemptRegister() {
        clearErrors();

        String email = getText(edtEmail);
        String password = getText(edtPassword);
        String confirmPassword = getText(edtConfirmPassword);
        String phone = getText(edtPhone);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        if (password.length() < 6) {
            tilPassword.setError("Mật khẩu >= 6 ký tự");
            return;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Mật khẩu không khớp");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Nhập số điện thoại");
            return;
        }

        btnRegister.setEnabled(false);

        authService.signUpWithEmailAndPassword(email, password, phone, new AuthCallback() {
            @Override
            public void onSuccess(User data) {
                showToast("Đăng ký thành công");
                AppNavigator.goToCustomerHome(RegisterActivity.this);
            }

            @Override
            public void onError(String message) {
                btnRegister.setEnabled(true);
                showToast(message);
            }
        });
    }

    private void signInWithFacebook() {
        FacebookAuthProviderConfig config = new FacebookAuthProviderConfig();

        LoginManager.getInstance().logInWithReadPermissions(
                this,
                config.getFacebookReadPermissions()
        );
    }

    private void signInWithGoogle() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                authService.signInWithGoogle(account.getIdToken(), new AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        AppNavigator.goToCustomerHome(RegisterActivity.this);
                    }

                    @Override
                    public void onError(String message) {
                        showToast(message);
                    }
                });

            } catch (ApiException e) {
                Log.e("GoogleAuth", "Login failed", e);
            }
        }
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
        tilPhone.setError(null);
    }

    @NonNull
    private String getText(TextInputEditText edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }
}