package com.example.cinemabooking.ui.customer.profile;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.util.Base64;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.AuthActivity;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.User;
import com.example.cinemabooking.service.ProfileService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EditProfileActivity extends AuthActivity {

    private TextInputEditText emailTV, birthdateTV, phoneInputTV, usernameInputTV;
    private MaterialButton saveChangesBtn, changePasswordBtn;
    private ImageView backBtn, profileAvatar;
    private MaterialCardView selectPhotoBtn, cameraIconBtn;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private FrameLayout loadingOverlay;

    private Uri s_profileUri;
    private ProfileService profileService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileService = ServiceProvider.getInstance().getProfileService();

        initViews();
        loadUserData();
        bindActions();
        handlePhotoPickerResult();
    }

    private void initViews() {
        emailTV = findViewById(R.id.edit_profile_email);
        birthdateTV = findViewById(R.id.edit_profile_birthdate);
        phoneInputTV = findViewById(R.id.edit_profile_phone);
        usernameInputTV = findViewById(R.id.edit_profile_username);

        saveChangesBtn = findViewById(R.id.edit_profile_save_change);
        changePasswordBtn = findViewById(R.id.btnChangePassword);
        backBtn = findViewById(R.id.edit_profile_back_btn);

        profileAvatar = findViewById(R.id.edit_profile_avatar);
        selectPhotoBtn = findViewById(R.id.edit_profile_photo_picker);
        cameraIconBtn = findViewById(R.id.edit_profile_camera_icon);

        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void bindActions() {
        backBtn.setOnClickListener(v -> finish());

        saveChangesBtn.setOnClickListener(v -> saveChanges());

        changePasswordBtn.setOnClickListener(v -> showChangePasswordDialog());

        // Make both the circle and the small camera icon trigger the photo picker
        selectPhotoBtn.setOnClickListener(v -> pickProfileAvatar());
        cameraIconBtn.setOnClickListener(v -> pickProfileAvatar());

        birthdateTV.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        // 1. Tạo ràng buộc: Chỉ cho chọn ngày cách đây tối đa 15 năm về trước
        Calendar constraintsCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        constraintsCalendar.add(Calendar.YEAR, -15);
        long maxDateInMillis = constraintsCalendar.getTimeInMillis();

        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.before(maxDateInMillis))
                .build();

        // 2. Khởi tạo MaterialDatePicker và cấu hình chế độ nhập phím trực tiếp
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Chọn ngày sinh")
                .setCalendarConstraints(constraints)
                .setSelection(maxDateInMillis)
                .setInputMode(MaterialDatePicker.INPUT_MODE_TEXT)
                .build();

        // 3. Lắng nghe sự kiện click nút OK
        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String formattedDate = sdf.format(new Date(selection));
            birthdateTV.setText(formattedDate);
        });

        // 4. Hiển thị Dialog
        datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
    }


    private void loadUserData() {
        User user = profileService.getCachedProfile();
        if (user == null) return;

        usernameInputTV.setText(user.name);
        phoneInputTV.setText(user.phone);
        birthdateTV.setText(user.birthDate);
        emailTV.setText(user.email);

        if ("Nam".equalsIgnoreCase(user.gender)) {
            rbMale.setChecked(true);
        } else if ("Nữ".equalsIgnoreCase(user.gender)) {
            rbFemale.setChecked(true);
        }
        //Kiểm tra có sẵn avatar chưa
        if (user.avatarUrl != null && user.avatarUrl.startsWith("data:image")) {
            String base64Content = user.avatarUrl.substring(user.avatarUrl.indexOf(",") + 1);
            byte[] imageBytes = Base64.decode(base64Content, Base64.DEFAULT);
            Glide.with(this)
                    .load(imageBytes)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .circleCrop()
                    .placeholder(R.drawable.user_solid_full)
                    .into(profileAvatar);
        } else {
            Glide.with(this)
                    .load(TextUtils.isEmpty(user.avatarUrl) ? R.drawable.user_solid_full : user.avatarUrl)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .circleCrop()
                    .placeholder(R.drawable.user_solid_full)
                    .into(profileAvatar);
        }

    }

    private void saveChanges() {
        String name = usernameInputTV.getText().toString().trim();
        String phone = phoneInputTV.getText().toString().trim();
        String birthDate = birthdateTV.getText().toString().trim();
        String gender = rbMale.isChecked() ? "Nam" : "Nữ";

        if (TextUtils.isEmpty(name)) {
            showToast("Vui lòng nhập họ tên");
            return;
        }

        if (!TextUtils.isEmpty(birthDate)) {
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
        }


        loadingOverlay.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            User cachedUser = profileService.getCachedProfile();
            if (cachedUser == null) return;

            User updatedUser = new User();
            updatedUser.uid = cachedUser.uid;
            updatedUser.email = cachedUser.email;
            updatedUser.role = cachedUser.role;

            updatedUser.name = name;
            updatedUser.phone = phone;
            updatedUser.birthDate = birthDate;
            updatedUser.gender = gender;

            if (s_profileUri != null) {
                String uploadedUrl = ServiceProvider.getInstance().getUploadService().uploadImage(s_profileUri);
                if (uploadedUrl != null) {
                    updatedUser.avatarUrl = uploadedUrl;
                } else {
                    android.util.Log.e("EditProfile", "Image upload returned null URL");
                    runOnUiThread(() -> {
                        loadingOverlay.setVisibility(View.GONE);
                        showToast("Lỗi tải ảnh lên. Vui lòng kiểm tra kết nối mạng.");
                    });
                    return;
                }
            } else {
                updatedUser.avatarUrl = cachedUser.avatarUrl;
            }

            runOnUiThread(() -> {
                profileService.updateUserProfile(updatedUser, new ResultCallback<User>() {
                    @Override
                    public void onSuccess(User data) {
                        loadingOverlay.setVisibility(View.GONE);
                        showToast("Cập nhật thông tin thành công");
                        finish();
                    }

                    @Override
                    public void onError(String message) {
                        loadingOverlay.setVisibility(View.GONE);
                        showToast("Lỗi: " + message);
                    }
                });
            });
        });
    }

    private void showChangePasswordDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.etConfirmPassword);

        new AlertDialog.Builder(this)
                .setTitle("Đổi mật khẩu")
                .setView(dialogView)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String oldPass = etOldPassword.getText().toString();
                    String newPass = etNewPassword.getText().toString();
                    String confirmPass = etConfirmPassword.getText().toString();

                    if (TextUtils.isEmpty(newPass) || newPass.length() < 6) {
                        showToast("Mật khẩu mới phải ít nhất 6 ký tự");
                        return;
                    }

                    if (!newPass.equals(confirmPass)) {
                        showToast("Mật khẩu xác nhận không khớp");
                        return;
                    }

                    loadingOverlay.setVisibility(View.VISIBLE);
                    ServiceProvider.getInstance().getAuthenticationService().updatePassword(newPass)
                            .addOnSuccessListener(aVoid -> {
                                loadingOverlay.setVisibility(View.GONE);
                                showToast("Đổi mật khẩu thành công");
                            })
                            .addOnFailureListener(e -> {
                                loadingOverlay.setVisibility(View.GONE);
                                showToast("Lỗi: " + e.getMessage());
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    private void handlePhotoPickerResult() {
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                s_profileUri = uri;
                Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .into(profileAvatar);
            }
        });
    }

    private void pickProfileAvatar() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
