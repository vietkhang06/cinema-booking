package com.example.cinemabooking.ui.auth;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.RadioButton;
import android.widget.TextView;

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
import com.example.cinemabooking.data.repository.UserRepositoryImpl;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.domain.common.AuthCallback;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.User;
import com.example.cinemabooking.domain.repository.UserRepository;
import com.example.cinemabooking.service.AuthenticationService;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends BaseActivity {

    private TextInputLayout tilEmail, tilPassword, tilConfirmPassword, tilPhone, tilFullName;
    private TextInputEditText edtEmail, edtPassword, edtConfirmPassword, edtPhone, edtFullName, edtBirthDate;
    private RadioButton rbMale, rbFemale;

    private MaterialButton btnRegister;
    private TextView tvBack;

    private MaterialCardView btnFacebook, btnGoogle;

    private AuthenticationService authService;
    private UserRepository userRepo;

    // Facebook
    private CallbackManager callbackManager;

    // Google (Credential Manager)
    private CredentialManager credentialManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = ServiceProvider.getInstance().getAuthenticationService();
        userRepo = new UserRepositoryImpl();

        credentialManager = CredentialManager.create(this);
        initViews();
        initFacebook();
        bindActions();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilPhone = findViewById(R.id.tilPhone);
        tilFullName = findViewById(R.id.tilFullName);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtPhone = findViewById(R.id.edtPhone);
        edtFullName = findViewById(R.id.edtFullName);
        edtBirthDate = findViewById(R.id.edtBirthDate);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);

        btnRegister = findViewById(R.id.btnRegister);
        tvBack = findViewById(R.id.tvBack);

        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
    }

    private void initFacebook() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<>() {
            @Override
            public void onSuccess(LoginResult result) {
                authService.handleFacebookAccessToken(result.getAccessToken(), new AuthCallback() {
                    @Override
                    public void onSuccess(User data) {
                        AppNavigator.goToHomeByRole(RegisterActivity.this, data.role);
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
            public void onError(@NonNull FacebookException error) {
                Log.e("FacebookAuth", String.valueOf(error.getMessage()));
            }
        });
    }

    private void bindActions() {
        tvBack.setOnClickListener(v -> AppNavigator.goToLogin(this));

        btnRegister.setOnClickListener(v -> attemptRegister());

        btnFacebook.setOnClickListener(v -> signInWithFacebook());

        btnGoogle.setOnClickListener(v -> signInWithGoogle());

        edtBirthDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -16); // Đặt mặc định hiển thị cách đây 16 năm
        long maxDate = c.getTimeInMillis();
        
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String formattedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    edtBirthDate.setText(formattedDate);
                },
                year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(maxDate); // Chặn chọn ngày lớn hơn (người dùng phải >= 16 tuổi)
        datePickerDialog.show();
    }

    private void attemptRegister() {
        clearErrors();

        String fullName = getText(edtFullName);
        String email = getText(edtEmail);
        String password = getText(edtPassword);
        String confirmPassword = getText(edtConfirmPassword);
        String phone = getText(edtPhone);
        String birthDate = getText(edtBirthDate);
        String gender = rbMale.isChecked() ? "Nam" : "Nữ";

        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError("Vui lòng nhập họ và tên");
            return;
        }

        if (TextUtils.isEmpty(birthDate)) {
            showToast("Vui lòng chọn ngày sinh");
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.US);
            sdf.setLenient(false);
            Date dateOfBirth = sdf.parse(birthDate);
            if (dateOfBirth != null) {
                Calendar dob = Calendar.getInstance();
                dob.setTime(dateOfBirth);
                Calendar today = Calendar.getInstance();

                int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }

                if (age < 16) {
                    showToast("Bạn phải từ 16 tuổi trở lên");
                    return;
                }
            }
        } catch (ParseException e) {
            showToast("Ngày sinh không đúng định dạng");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError("Vui lòng nhập email");
            return;
        }

        boolean isValidEmail = Patterns.EMAIL_ADDRESS.matcher(email).matches() && 
                (email.endsWith("@gmail.com") || email.endsWith("@gm.uit.edu.vn") || email.endsWith("@uit.edu.vn"));
        if (!isValidEmail) {
            tilEmail.setError("Email không hợp lệ (chỉ chấp nhận Gmail / email UIT)");
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

        authService.signUpWithEmailAndPassword(email, password, phone, fullName, new AuthCallback() {
            @Override
            public void onSuccess(User data) {
                data.birthDate = birthDate;
                data.gender = gender;
                userRepo.updateUser(data, new ResultCallback<User>() {
                    @Override
                    public void onSuccess(User updatedUser) {
                        showToast("Đăng ký thành công! Vui lòng đăng nhập.");
                        AppNavigator.goToLogin(RegisterActivity.this);
                    }

                    @Override
                    public void onError(String message) {
                        btnRegister.setEnabled(true);
                        showToast("Lỗi cập nhật thông tin: " + message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                btnRegister.setEnabled(true);
                showToast(message);
            }
        });
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logIn(this, callbackManager,
                Arrays.asList("email", "public_profile"));
    }

    private void signInWithGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        credentialManager.getCredentialAsync(
                this, request, null,
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleGoogleCredential(result.getCredential());
                    }

                    @Override
                    public void onError(@NonNull GetCredentialException e) {
                        showToast("Đăng nhập Google thất bại. Vui lòng thử lại.");
                    }
                }
        );
    }

    private void handleGoogleCredential(Credential credential) {
        if (!(credential instanceof GoogleIdTokenCredential)) {
            showToast("Không thể đăng nhập Google");
            return;
        }
        String idToken = ((GoogleIdTokenCredential) credential).getIdToken();
        authService.signInWithGoogle(idToken, new AuthCallback() {
            @Override
            public void onSuccess(User user) {
                AppNavigator.goToHomeByRole(RegisterActivity.this, user.role);
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void clearErrors() {
        if (tilFullName != null) {
            tilFullName.setError(null);
        }
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