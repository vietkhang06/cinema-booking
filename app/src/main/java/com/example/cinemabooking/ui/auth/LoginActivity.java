package com.example.cinemabookingapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;
import com.example.cinemabookingapp.di.ServiceProvider;
import com.example.cinemabookingapp.domain.common.AuthCallback;
import com.example.cinemabookingapp.domain.model.User;
import com.example.cinemabookingapp.service.AuthenticationService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

public class LoginActivity extends BaseActivity {

    private static final int RC_SIGN_IN = 1001;

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText edtEmail, edtPassword;
    private MaterialButton btnLogin;
    private MaterialCheckBox cbRemember;

    private AuthenticationService authService;
    private GoogleSignInClient googleSignInClient;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = ServiceProvider
                .getInstance(getApplicationContext())
                .getAuthenticationService();

        initViews();
        initGoogle();
        initFacebook();
        bindActions();
        loadRememberedEmail();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        cbRemember = findViewById(R.id.cbRemember);
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
    }

    private void bindActions() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        findViewById(R.id.tvRegister)
                .setOnClickListener(v -> AppNavigator.goToRegister(this));

        findViewById(R.id.tvForgotPassword)
                .setOnClickListener(v -> AppNavigator.goToForgotPassword(this));

        findViewById(R.id.btnGoogle)
                .setOnClickListener(v -> startGoogleLogin());

        findViewById(R.id.btnFacebook)
                .setOnClickListener(v -> startFacebookLogin());

        findViewById(R.id.btnApple)
                .setOnClickListener(v -> showToast("Apple chưa hỗ trợ"));
    }

    private void loadRememberedEmail() {
        String saved = sessionManager.getRememberedEmail();
        if (!saved.isEmpty()) {
            edtEmail.setText(saved);
            cbRemember.setChecked(true);
        }
    }

    private void attemptLogin() {
        clearErrors();

        String email = getText(edtEmail);
        String password = getText(edtPassword);

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Nhập email");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email không hợp lệ");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError("Nhập mật khẩu");
            return;
        }

        btnLogin.setEnabled(false);

        authService.signInWithEmailAndPassword(
                email,
                password,
                cbRemember.isChecked(),
                new AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        btnLogin.setEnabled(true);
                        AppNavigator.goToHomeByRole(LoginActivity.this, user.role);
                    }

                    @Override
                    public void onError(String message) {
                        btnLogin.setEnabled(true);
                        showToast(message);
                    }
                }
        );
    }

    private void startGoogleLogin() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, RC_SIGN_IN);
    }

    private void startFacebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(
                this,
                Arrays.asList("email", "public_profile")
        );

        LoginManager.getInstance().registerCallback(
                callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult result) {
                        authService.handleFacebookAccessToken(
                                result.getAccessToken(),
                                new AuthCallback() {
                                    @Override
                                    public void onSuccess(User user) {
                                        AppNavigator.goToHomeByRole(LoginActivity.this, user.role);
                                    }

                                    @Override
                                    public void onError(String message) {
                                        showToast(message);
                                    }
                                }
                        );
                    }

                    @Override
                    public void onCancel() {
                        showToast("Huỷ đăng nhập Facebook");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        showToast(error.getMessage());
                    }
                }
        );
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    @NonNull
    private String getText(TextInputEditText edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account == null || account.getIdToken() == null) {
                    showToast("Google login failed");
                    return;
                }

                authService.signInWithGoogle(account.getIdToken(), new AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        AppNavigator.goToHomeByRole(LoginActivity.this, user.role);
                    }

                    @Override
                    public void onError(String message) {
                        showToast(message);
                    }
                });

            } catch (ApiException e) {
                showToast("Google login failed");
            }
        }
    }
}