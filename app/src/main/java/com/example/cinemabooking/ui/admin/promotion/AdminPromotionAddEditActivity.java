package com.example.cinemabooking.ui.admin.promotion;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.repository.PromotionRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Promotion;
import com.example.cinemabooking.ui.admin.log.AdminAuditLogger;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminPromotionAddEditActivity extends AppCompatActivity {

    private EditText etCode, etTitle, etDesc, etValue, etMaxDiscount, etMinAmount, etUsageLimit;
    private Spinner spinnerDiscountType, spinnerTargetRole;
    private TextView tvValidFromDate, tvValidFromTime, tvValidToDate, tvValidToTime;
    private SwitchCompat switchStatus;
    private Button btnSave, btnDelete;
    private TextView tvFormTitle;
    private View layoutMaxDiscount;

    private PromotionRepositoryImpl promotionRepository;
    private String promoIdToEdit = null;
    private Promotion existingPromo = null;

    private final Calendar calendarFrom = Calendar.getInstance();
    private final Calendar calendarTo = Calendar.getInstance();

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_promotion_add_edit);

        promotionRepository = new PromotionRepositoryImpl();

        // Get bundle data
        if (getIntent() != null && getIntent().hasExtra("PROMO_ID")) {
            promoIdToEdit = getIntent().getStringExtra("PROMO_ID");
        }

        initViews();
        setupSpinners();
        setupDateTimePickers();
        setupListeners();

        if (promoIdToEdit != null) {
            loadPromotionData();
        } else {
            // Default: validFrom = now, validTo = now + 7 days
            calendarTo.add(Calendar.DAY_OF_YEAR, 7);
            updateDateTimeLabels();
        }
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnPromoFormBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvFormTitle = findViewById(R.id.tvPromoFormTitle);
        etCode = findViewById(R.id.etPromoCodeInput);
        etTitle = findViewById(R.id.etPromoTitleInput);
        etDesc = findViewById(R.id.etPromoDescInput);
        etValue = findViewById(R.id.etPromoValueInput);
        etMaxDiscount = findViewById(R.id.etPromoMaxDiscountInput);
        etMinAmount = findViewById(R.id.etPromoMinAmountInput);
        etUsageLimit = findViewById(R.id.etPromoUsageLimitInput);
        
        spinnerDiscountType = findViewById(R.id.spinnerDiscountType);
        spinnerTargetRole = findViewById(R.id.spinnerTargetRole);
        layoutMaxDiscount = findViewById(R.id.layoutMaxDiscount);

        tvValidFromDate = findViewById(R.id.tvValidFromDate);
        tvValidFromTime = findViewById(R.id.tvValidFromTime);
        tvValidToDate = findViewById(R.id.tvValidToDate);
        tvValidToTime = findViewById(R.id.tvValidToTime);

        switchStatus = findViewById(R.id.switchPromoStatus);
        btnSave = findViewById(R.id.btnSavePromo);
        btnDelete = findViewById(R.id.btnDeletePromo);
    }

    private void setupSpinners() {
        // Discount Type Spinner
        String[] discountTypes = {"Phần trăm (%)", "Số tiền cố định (đ)"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, discountTypes);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDiscountType.setAdapter(typeAdapter);

        spinnerDiscountType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // If Percentage, show Max Discount layout, else hide
                if (position == 0) {
                    layoutMaxDiscount.setVisibility(View.VISIBLE);
                } else {
                    layoutMaxDiscount.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Target Member Level Spinner
        String[] targetLevels = {"Tất cả", "Hạng Standard", "Hạng VIP", "Hạng Gold"};
        ArrayAdapter<String> targetAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, targetLevels);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTargetRole.setAdapter(targetAdapter);
    }

    private void setupDateTimePickers() {
        // From Date
        tvValidFromDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày bắt đầu")
                    .setSelection(calendarFrom.getTimeInMillis())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(selection);
                calendarFrom.set(Calendar.YEAR, c.get(Calendar.YEAR));
                calendarFrom.set(Calendar.MONTH, c.get(Calendar.MONTH));
                calendarFrom.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
                updateDateTimeLabels();
            });
            datePicker.show(getSupportFragmentManager(), "FROM_DATE_PICKER");
        });

        // From Time
        tvValidFromTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(calendarFrom.get(Calendar.HOUR_OF_DAY))
                    .setMinute(calendarFrom.get(Calendar.MINUTE))
                    .setTitleText("Chọn giờ bắt đầu")
                    .build();
            timePicker.addOnPositiveButtonClickListener(view -> {
                calendarFrom.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendarFrom.set(Calendar.MINUTE, timePicker.getMinute());
                calendarFrom.set(Calendar.SECOND, 0);
                calendarFrom.set(Calendar.MILLISECOND, 0);
                updateDateTimeLabels();
            });
            timePicker.show(getSupportFragmentManager(), "FROM_TIME_PICKER");
        });

        // To Date
        tvValidToDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Chọn ngày kết thúc")
                    .setSelection(calendarTo.getTimeInMillis())
                    .build();
            datePicker.addOnPositiveButtonClickListener(selection -> {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(selection);
                calendarTo.set(Calendar.YEAR, c.get(Calendar.YEAR));
                calendarTo.set(Calendar.MONTH, c.get(Calendar.MONTH));
                calendarTo.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
                updateDateTimeLabels();
            });
            datePicker.show(getSupportFragmentManager(), "TO_DATE_PICKER");
        });

        // To Time
        tvValidToTime.setOnClickListener(v -> {
            MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(calendarTo.get(Calendar.HOUR_OF_DAY))
                    .setMinute(calendarTo.get(Calendar.MINUTE))
                    .setTitleText("Chọn giờ kết thúc")
                    .build();
            timePicker.addOnPositiveButtonClickListener(view -> {
                calendarTo.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                calendarTo.set(Calendar.MINUTE, timePicker.getMinute());
                calendarTo.set(Calendar.SECOND, 0);
                calendarTo.set(Calendar.MILLISECOND, 0);
                updateDateTimeLabels();
            });
            timePicker.show(getSupportFragmentManager(), "TO_TIME_PICKER");
        });
    }

    private void updateDateTimeLabels() {
        tvValidFromDate.setText(dateFormat.format(calendarFrom.getTime()));
        tvValidFromTime.setText(timeFormat.format(calendarFrom.getTime()));
        tvValidToDate.setText(dateFormat.format(calendarTo.getTime()));
        tvValidToTime.setText(timeFormat.format(calendarTo.getTime()));
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> savePromotion());

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa khuyến mãi")
                    .setMessage("Bạn có chắc muốn xóa khuyến mãi này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> deletePromotion())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void loadPromotionData() {
        tvFormTitle.setText("Chỉnh sửa khuyến mãi");
        btnDelete.setVisibility(View.VISIBLE);
        etCode.setEnabled(false); // In edit mode, we shouldn't modify the code

        promotionRepository.getPromotionById(promoIdToEdit, new ResultCallback<Promotion>() {
            @Override
            public void onSuccess(Promotion promotion) {
                existingPromo = promotion;
                etCode.setText(promotion.code);
                etTitle.setText(promotion.title);
                etDesc.setText(promotion.description);
                etValue.setText(String.valueOf(promotion.discountValue));
                etMaxDiscount.setText(String.valueOf((int)promotion.maxDiscountAmount));
                etMinAmount.setText(String.valueOf((int)promotion.minAmount));
                etUsageLimit.setText(String.valueOf(promotion.usageLimit));

                // Spinners
                if ("percentage".equalsIgnoreCase(promotion.discountType)) {
                    spinnerDiscountType.setSelection(0);
                    layoutMaxDiscount.setVisibility(View.VISIBLE);
                } else {
                    spinnerDiscountType.setSelection(1);
                    layoutMaxDiscount.setVisibility(View.GONE);
                }

                String target = promotion.targetRole != null ? promotion.targetRole.toLowerCase() : "all";
                if ("standard".equals(target)) {
                    spinnerTargetRole.setSelection(1);
                } else if ("vip".equals(target)) {
                    spinnerTargetRole.setSelection(2);
                } else if ("gold".equals(target)) {
                    spinnerTargetRole.setSelection(3);
                } else {
                    spinnerTargetRole.setSelection(0);
                }

                // Dates
                calendarFrom.setTimeInMillis(promotion.validFrom);
                calendarTo.setTimeInMillis(promotion.validTo);
                updateDateTimeLabels();

                // Status
                switchStatus.setChecked("active".equalsIgnoreCase(promotion.status));
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminPromotionAddEditActivity.this, "Không tìm thấy dữ liệu: " + message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void savePromotion() {
        String code = etCode.getText().toString().trim().toUpperCase(Locale.getDefault());
        String title = etTitle.getText().toString().trim();
        String desc = etDesc.getText().toString().trim();
        String valueStr = etValue.getText().toString().trim();
        String maxDiscountStr = etMaxDiscount.getText().toString().trim();
        String minAmountStr = etMinAmount.getText().toString().trim();
        String usageLimitStr = etUsageLimit.getText().toString().trim();

        if (code.isEmpty()) {
            etCode.setError("Vui lòng nhập mã khuyến mãi");
            return;
        }
        if (title.isEmpty()) {
            etTitle.setError("Vui lòng nhập tiêu đề");
            return;
        }
        if (valueStr.isEmpty()) {
            etValue.setError("Vui lòng nhập giá trị giảm");
            return;
        }

        double val = Double.parseDouble(valueStr);
        String discountType = spinnerDiscountType.getSelectedItemPosition() == 0 ? "percentage" : "fixed";

        if ("percentage".equals(discountType) && (val <= 0 || val > 100)) {
            etValue.setError("Phần trăm giảm phải từ 1% đến 100%");
            return;
        }
        if ("fixed".equals(discountType) && val <= 0) {
            etValue.setError("Số tiền giảm phải lớn hơn 0đ");
            return;
        }

        final double maxDisc;
        if ("percentage".equals(discountType)) {
            if (maxDiscountStr.isEmpty()) {
                etMaxDiscount.setError("Vui lòng nhập số tiền giảm tối đa");
                return;
            }
            maxDisc = Double.parseDouble(maxDiscountStr);
        } else {
            maxDisc = 0;
        }

        double minAmount = minAmountStr.isEmpty() ? 0 : Double.parseDouble(minAmountStr);
        int usageLimit = usageLimitStr.isEmpty() ? 999999 : Integer.parseInt(usageLimitStr);

        long validFrom = calendarFrom.getTimeInMillis();
        long validTo = calendarTo.getTimeInMillis();

        if (validFrom >= validTo) {
            Toast.makeText(this, "Ngày bắt đầu phải trước ngày kết thúc", Toast.LENGTH_SHORT).show();
            return;
        }

        final String targetRole;
        int targetPos = spinnerTargetRole.getSelectedItemPosition();
        if (targetPos == 1) targetRole = "standard";
        else if (targetPos == 2) targetRole = "vip";
        else if (targetPos == 3) targetRole = "gold";
        else targetRole = "all";

        String status = switchStatus.isChecked() ? "active" : "inactive";

        // Create or Update
        if (promoIdToEdit == null) {
            // Check if code is unique
            promotionRepository.getPromotionByCode(code, new ResultCallback<Promotion>() {
                @Override
                public void onSuccess(Promotion promotion) {
                    etCode.setError("Mã khuyến mãi này đã tồn tại");
                    Toast.makeText(AdminPromotionAddEditActivity.this, "Mã khuyến mãi này đã tồn tại trên hệ thống", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String message) {
                    // Unique code, proceed to create
                    Promotion newPromo = new Promotion();
                    newPromo.code = code;
                    newPromo.title = title;
                    newPromo.description = desc;
                    newPromo.discountType = discountType;
                    newPromo.discountValue = val;
                    newPromo.maxDiscountAmount = maxDisc;
                    newPromo.minAmount = minAmount;
                    newPromo.usageLimit = usageLimit;
                    newPromo.usedCount = 0;
                    newPromo.targetRole = targetRole;
                    newPromo.validFrom = validFrom;
                    newPromo.validTo = validTo;
                    newPromo.status = status;
                    newPromo.deleted = false;
                    newPromo.createdAt = System.currentTimeMillis();
                    newPromo.updatedAt = System.currentTimeMillis();

                    promotionRepository.createPromotion(newPromo, new ResultCallback<Promotion>() {
                        @Override
                        public void onSuccess(Promotion created) {
                            Toast.makeText(AdminPromotionAddEditActivity.this, "Tạo khuyến mãi thành công", Toast.LENGTH_SHORT).show();
                            
                            AdminAuditLogger.log(
                                    "CREATE_PROMOTION",
                                    "Promotion",
                                    created.promoId,
                                    "Tạo mới mã khuyến mãi: " + created.code + " (" + created.title + ")"
                            );
                            
                            finish();
                        }

                        @Override
                        public void onError(String err) {
                            Toast.makeText(AdminPromotionAddEditActivity.this, "Lỗi: " + err, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        } else {
            // Update mode
            existingPromo.title = title;
            existingPromo.description = desc;
            existingPromo.discountType = discountType;
            existingPromo.discountValue = val;
            existingPromo.maxDiscountAmount = maxDisc;
            existingPromo.minAmount = minAmount;
            existingPromo.usageLimit = usageLimit;
            existingPromo.targetRole = targetRole;
            existingPromo.validFrom = validFrom;
            existingPromo.validTo = validTo;
            existingPromo.status = status;
            existingPromo.updatedAt = System.currentTimeMillis();

            promotionRepository.updatePromotion(existingPromo, new ResultCallback<Promotion>() {
                @Override
                public void onSuccess(Promotion updated) {
                    Toast.makeText(AdminPromotionAddEditActivity.this, "Cập nhật khuyến mãi thành công", Toast.LENGTH_SHORT).show();
                    
                    AdminAuditLogger.log(
                            "UPDATE_PROMOTION",
                            "Promotion",
                            updated.promoId,
                            "Cập nhật mã khuyến mãi: " + updated.code + " (" + updated.title + ")"
                    );
                    
                    finish();
                }

                @Override
                public void onError(String err) {
                    Toast.makeText(AdminPromotionAddEditActivity.this, "Lỗi: " + err, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deletePromotion() {
        promotionRepository.softDeletePromotion(promoIdToEdit, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(AdminPromotionAddEditActivity.this, "Đã xóa khuyến mãi", Toast.LENGTH_SHORT).show();
                
                AdminAuditLogger.log(
                        "DELETE_PROMOTION",
                        "Promotion",
                        promoIdToEdit,
                        "Xóa mã khuyến mãi: " + (existingPromo != null ? existingPromo.code : promoIdToEdit)
                );
                
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminPromotionAddEditActivity.this, "Lỗi khi xóa: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
