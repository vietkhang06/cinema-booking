package com.example.cinemabooking.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.core.navigation.AppNavigator;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.domain.common.AuthCallback;
import com.example.cinemabooking.domain.model.User;
import com.example.cinemabooking.service.AuthenticationService;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;

public class LoginActivity extends BaseActivity {

    /** Khi true: login xong thì finish() về màn hình trước thay vì go to Home */
    public static final String EXTRA_FROM_BOOKING = "from_booking";

    private static final String TAG = "GOOGLE_LOGIN";

    private boolean fromBooking = false;

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText edtEmail, edtPassword;
    private MaterialButton btnLogin;
    private MaterialCheckBox cbRemember;

    private AuthenticationService authService;
    private CredentialManager credentialManager;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fromBooking = getIntent().getBooleanExtra(EXTRA_FROM_BOOKING, false);

        authService = ServiceProvider
                .getInstance(getApplicationContext())
                .getAuthenticationService();

        credentialManager = CredentialManager.create(this);
        initViews();
        initFacebook();
        bindActions();
        loadRememberedCredentials();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        cbRemember = findViewById(R.id.cbRemember);
    }

    private void initFacebook() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<>() {
            @Override
            public void onSuccess(LoginResult result) {

                Log.d("FACEBOOK_LOGIN",
                        "Facebook Login Success");

                Log.d("FACEBOOK_LOGIN",
                        "Token = "
                                + result.getAccessToken().getToken());
                authService.handleFacebookAccessToken(result.getAccessToken(), new AuthCallback() {
                    @Override
                    public void onSuccess(User user) {
                        if ("admin".equals(user.role)) {
                            AppNavigator.goToHomeByRole(LoginActivity.this, user.role);
                        } else if (fromBooking) {
                            finish();
                        } else {
                            Log.e("ROLE_DEBUG", "EMAIL=" + user.getEmail() + " ROLE=" + user.getRole());
                            AppNavigator.goToHomeByRole(LoginActivity.this, user.role);
                        }
                    }

                    @Override
                    public void onError(String message) {
                        showToast(message);
                    }
                });
            }

            @Override
            public void onCancel() {
                showToast("Huỷ đăng nhập Facebook");
            }

            @Override
            public void onError(@NonNull FacebookException error) {
                showToast(String.valueOf(error.getMessage()));
            }
        });
    }

    private void bindActions() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        findViewById(R.id.tvRegister)
                .setOnClickListener(v -> AppNavigator.goToRegister(this));

        findViewById(R.id.tvForgotPassword)
                .setOnClickListener(v -> AppNavigator.goToForgotPassword(this));

        findViewById(R.id.btnGoogle)
                .setOnClickListener(v -> {
            Log.d("GOOGLE_LOGIN", "User clicked Google Login");
            startGoogleLogin();
        });

        findViewById(R.id.btnFacebook)
                .setOnClickListener(v -> startFacebookLogin());

        findViewById(R.id.btnApple)
                .setOnClickListener(v -> showToast("Apple chưa hỗ trợ"));
    }

    /** Điền sẵn email + password nếu người dùng đã tick "Nhớ mật khẩu" lần trước */
    private void loadRememberedCredentials() {
        String savedEmail    = sessionManager.getRememberedEmail();
        String savedPassword = sessionManager.getRememberedPassword();
        if (!savedEmail.isEmpty()) {
            edtEmail.setText(savedEmail);
            cbRemember.setChecked(true);
        }
        if (!savedPassword.isEmpty()) {
            edtPassword.setText(savedPassword);
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

                        if (cbRemember.isChecked()) {
                            sessionManager.saveRememberedEmail(email);
                            sessionManager.saveRememberedPassword(password);
                        } else {
                            sessionManager.clearRememberedEmail();
                            sessionManager.clearRememberedPassword();
                        }

                        if ("admin".equals(user.role) || "staff".equals(user.role)) {
                            AppNavigator.goToHomeByRole(LoginActivity.this, user.role);
                        } else if (fromBooking) {
                            finish();
                        } else {
                            AppNavigator.goToHomeByRole(LoginActivity.this, user.role);
                        }
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

        Log.d(TAG, "========== START GOOGLE LOGIN ==========");

        try {

            Log.d(TAG, "Creating GoogleIdOption");

            GetGoogleIdOption googleIdOption =
                    new GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(
                                    getString(R.string.default_web_client_id)
                            )
                            .build();

            Log.d(TAG, "Server Client Id = "
                    + getString(R.string.default_web_client_id));

            GetCredentialRequest request =
                    new GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build();

            Log.d(TAG, "Calling CredentialManager");

            credentialManager.getCredentialAsync(
                    this,
                    request,
                    null,
                    ContextCompat.getMainExecutor(this),
                    new CredentialManagerCallback<
                            GetCredentialResponse,
                            GetCredentialException>() {

                        @Override
                        public void onResult(GetCredentialResponse result) {

                            Log.d(TAG,
                                    "CredentialManager SUCCESS");

                            if (result == null) {

                                Log.e(TAG,
                                        "GetCredentialResponse is NULL");

                                showToast("Credential response null");
                                return;
                            }

                            handleGoogleCredential(
                                    result.getCredential()
                            );
                        }

                        @Override
                        public void onError(
                                @NonNull GetCredentialException e) {

                            Log.e(TAG,
                                    "CredentialManager ERROR",
                                    e);

                            showToast(
                                    "Đăng nhập Google thất bại"
                            );
                        }
                    }
            );

        } catch (Exception e) {

            Log.e(TAG,
                    "START GOOGLE LOGIN EXCEPTION",
                    e);

            showToast("Google Login Exception");
        }
    }

    private void handleGoogleCredential(
            Credential credential) {

        Log.d(TAG,
                "========== HANDLE CREDENTIAL ==========");

        if (credential == null) {

            Log.e(TAG,
                    "Credential is NULL");

            return;
        }

        Log.d(TAG,
                "Credential Class = "
                        + credential.getClass().getName());

        Log.d(TAG,
                "Credential Type = "
                        + credential.getType());

        try {

            if (!credential.getType().equals(
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            )) {

                Log.e(TAG,
                        "Not GoogleIdTokenCredential");

                showToast(
                        "Không nhận được Google ID Token"
                );

                return;
            }

            GoogleIdTokenCredential googleCredential =
                    GoogleIdTokenCredential.createFrom(
                            credential.getData()
                    );

            String idToken =
                    googleCredential.getIdToken();

            Log.d(TAG,
                    "ID TOKEN RECEIVED");

            Log.d(TAG,
                    "Token Length = "
                            + (idToken == null
                            ? 0
                            : idToken.length()));

            if (idToken == null ||
                    idToken.isEmpty()) {

                Log.e(TAG,
                        "ID TOKEN EMPTY");

                showToast("Google token rỗng");

                return;
            }

            Log.d(TAG,
                    "Calling Firebase Login");

            authService.signInWithGoogle(
                    idToken,
                    new AuthCallback() {

                        @Override
                        public void onSuccess(User user) {

                            Log.d(TAG,
                                    "Firebase Login SUCCESS");

                            if (user != null) {

                                Log.d(TAG,
                                        "UID = " + user.uid);

                                Log.d(TAG,
                                        "EMAIL = "
                                                + user.email);

                                Log.d(TAG,
                                        "ROLE = "
                                                + user.role);
                            }

                            if ("admin".equals(user.role)) {
                                AppNavigator.goToHomeByRole(LoginActivity.this, user.role);
                            } else if (fromBooking) {
                                finish();
                            } else {
                                AppNavigator.goToHomeByRole(LoginActivity.this, user.role);
                            }
                        }

                        @Override
                        public void onError(String message) {

                            Log.e(TAG,
                                    "Firebase Login FAILED");

                            Log.e(TAG,
                                    "Error = "
                                            + message);

                            showToast(message);
                        }
                    });

        } catch (Exception e) {

            Log.e(TAG,
                    "HANDLE CREDENTIAL ERROR",
                    e);
            showToast(
                    "Lỗi xử lý Google Credential"
            );
        }
    }

    private void startFacebookLogin() {
        LoginManager.getInstance().logIn(this, callbackManager,
                Arrays.asList("email", "public_profile"));
    }

    private void clearErrors() {
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    @NonNull
    private String getText(TextInputEditText edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }

}