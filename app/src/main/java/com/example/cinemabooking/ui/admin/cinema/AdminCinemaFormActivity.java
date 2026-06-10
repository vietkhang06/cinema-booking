package com.example.cinemabooking.ui.admin.cinema;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.data.repository.CinemaRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AdminCinemaFormActivity extends BaseActivity {

    public static final String EXTRA_CINEMA_ID = "extra_cinema_id";

    private TextInputLayout tilName, tilAddress, tilCity, tilDistrict, tilPhone, tilLat, tilLng, tilStatus;
    private TextInputEditText edtName, edtAddress, edtCity, edtDistrict, edtPhone, edtLat, edtLng;
    private MaterialAutoCompleteTextView actvStatus;
    private MaterialButton btnSave;

    private CinemaRepositoryImpl cinemaRepository;
    private Cinema currentCinema;
    private boolean isEditMode = false;
    private String cinemaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_cinema_form);

        cinemaRepository = new CinemaRepositoryImpl();
        cinemaId = getIntent().getStringExtra(EXTRA_CINEMA_ID);
        isEditMode = !TextUtils.isEmpty(cinemaId);

        initViews();
        setupStatusDropdown();
        bindActions();

        TextView tvFormTitle = findViewById(R.id.tvFormTitle);
        if (isEditMode) {
            btnSave.setText("Cập nhật rạp");
            if (tvFormTitle != null) tvFormTitle.setText("Cập nhật rạp chiếu");
            loadCinema();
        } else {
            btnSave.setText("Thêm rạp");
            if (tvFormTitle != null) tvFormTitle.setText("Thêm rạp mới");
        }
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        tilAddress = findViewById(R.id.tilAddress);
        tilCity = findViewById(R.id.tilCity);
        tilDistrict = findViewById(R.id.tilDistrict);
        tilPhone = findViewById(R.id.tilPhone);
        tilLat = findViewById(R.id.tilLat);
        tilLng = findViewById(R.id.tilLng);
        tilStatus = findViewById(R.id.tilStatus);

        edtName = findViewById(R.id.edtName);
        edtAddress = findViewById(R.id.edtAddress);
        edtCity = findViewById(R.id.edtCity);
        edtDistrict = findViewById(R.id.edtDistrict);
        edtPhone = findViewById(R.id.edtPhone);
        edtLat = findViewById(R.id.edtLat);
        edtLng = findViewById(R.id.edtLng);

        actvStatus = findViewById(R.id.actvStatus);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupStatusDropdown() {
        String[] statuses = {"active", "inactive"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statuses
        );
        actvStatus.setAdapter(adapter);
    }

    private void bindActions() {
        btnSave.setOnClickListener(v -> saveCinema());
        
        android.view.View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        android.view.View btnMapPicker = findViewById(R.id.btnMapPickerSim);
        if (btnMapPicker != null) {
            btnMapPicker.setOnClickListener(v -> showMapSimDialog());
        }
    }

    private void showMapSimDialog() {
        String[] locations = {"Hồ Chí Minh (10.7626, 106.6829)", "Hà Nội (21.0285, 105.8542)", "Đà Nẵng (16.0544, 108.2022)"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("📍 Mô phỏng chọn vị trí trên Google Maps")
                .setItems(locations, (dialog, which) -> {
                    if (which == 0) {
                        edtLat.setText("10.7626");
                        edtLng.setText("106.6829");
                        showToast("Đã ghim vị trí Hồ Chí Minh");
                    } else if (which == 1) {
                        edtLat.setText("21.0285");
                        edtLng.setText("105.8542");
                        showToast("Đã ghim vị trí Hà Nội");
                    } else if (which == 2) {
                        edtLat.setText("16.0544");
                        edtLng.setText("108.2022");
                        showToast("Đã ghim vị trí Đà Nẵng");
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void loadCinema() {
        cinemaRepository.getCinemaById(cinemaId, new ResultCallback<Cinema>() {
            @Override
            public void onSuccess(Cinema data) {
                if (data == null) {
                    showToast("Không tìm thấy rạp");
                    finish();
                    return;
                }
                currentCinema = data;
                bindCinema(data);
            }

            @Override
            public void onError(String message) {
                showToast(message);
                finish();
            }
        });
    }

    private void bindCinema(Cinema cinema) {
        edtName.setText(cinema.name);
        edtAddress.setText(cinema.address);
        edtCity.setText(cinema.city);
        edtDistrict.setText(cinema.district);
        edtPhone.setText(cinema.phone);
        edtLat.setText(String.valueOf(cinema.latitude));
        edtLng.setText(String.valueOf(cinema.longitude));
        actvStatus.setText(safe(cinema.status), false);
    }

    private void saveCinema() {
        clearErrors();

        String name = getText(edtName);
        String address = getText(edtAddress);
        String city = getText(edtCity);
        String district = getText(edtDistrict);
        String phone = getText(edtPhone);
        String latText = getText(edtLat);
        String lngText = getText(edtLng);
        String status = getText(actvStatus);

        if (TextUtils.isEmpty(name)) {
            tilName.setError("Nhập tên rạp");
            return;
        }

        if (TextUtils.isEmpty(address)) {
            tilAddress.setError("Nhập địa chỉ");
            return;
        }

        if (TextUtils.isEmpty(city)) {
            tilCity.setError("Nhập tỉnh/thành");
            return;
        }

        if (TextUtils.isEmpty(district)) {
            tilDistrict.setError("Nhập quận/huyện");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            tilPhone.setError("Nhập số điện thoại");
            return;
        }

        double latitude = 0;
        double longitude = 0;

        if (!TextUtils.isEmpty(latText)) {
            try {
                latitude = Double.parseDouble(latText);
            } catch (NumberFormatException e) {
                tilLat.setError("Vĩ độ không hợp lệ");
                return;
            }
        }

        if (!TextUtils.isEmpty(lngText)) {
            try {
                longitude = Double.parseDouble(lngText);
            } catch (NumberFormatException e) {
                tilLng.setError("Kinh độ không hợp lệ");
                return;
            }
        }

        Cinema cinema = (isEditMode && currentCinema != null) ? currentCinema : new Cinema();

        if (isEditMode) {
            cinema.cinemaId = cinemaId;
        }

        cinema.name = name;
        cinema.address = address;
        cinema.city = city;
        cinema.district = district;
        cinema.phone = phone;
        cinema.latitude = latitude;
        cinema.longitude = longitude;
        cinema.status = TextUtils.isEmpty(status) ? "active" : status;

        if (!isEditMode || currentCinema == null) {
            cinema.deleted = false;
            cinema.createdAt = System.currentTimeMillis();
        } else {
            cinema.deleted = currentCinema.deleted;
            cinema.createdAt = currentCinema.createdAt;
        }

        cinema.updatedAt = System.currentTimeMillis();

        btnSave.setEnabled(false);

        if (isEditMode) {
            cinemaRepository.updateCinema(cinema, new ResultCallback<Cinema>() {
                @Override
                public void onSuccess(Cinema data) {
                    com.example.cinemabooking.ui.admin.log.AdminAuditLogger.log("UPDATE_CINEMA", "CINEMA", data.cinemaId, "Đã cập nhật thông tin rạp: " + data.name);
                    showToast("Đã cập nhật rạp");
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnSave.setEnabled(true);
                    showToast(message);
                }
            });
        } else {
            cinemaRepository.createCinema(cinema, new ResultCallback<Cinema>() {
                @Override
                public void onSuccess(Cinema data) {
                    com.example.cinemabooking.ui.admin.log.AdminAuditLogger.log("CREATE_CINEMA", "CINEMA", data.cinemaId, "Đã thêm rạp chiếu mới: " + data.name);
                    showToast("Đã thêm rạp");
                    finish();
                }

                @Override
                public void onError(String message) {
                    btnSave.setEnabled(true);
                    showToast(message);
                }
            });
        }
    }

    private void clearErrors() {
        tilName.setError(null);
        tilAddress.setError(null);
        tilCity.setError(null);
        tilDistrict.setError(null);
        tilPhone.setError(null);
        tilLat.setError(null);
        tilLng.setError(null);
        tilStatus.setError(null);
    }

    private String getText(TextInputEditText edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }

    private String getText(MaterialAutoCompleteTextView edt) {
        return edt.getText() == null ? "" : edt.getText().toString().trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}