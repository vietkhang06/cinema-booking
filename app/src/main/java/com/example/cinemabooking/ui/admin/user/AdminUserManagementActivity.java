package com.example.cinemabooking.ui.admin.user;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.repository.UserRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.User;
import com.example.cinemabooking.ui.admin.log.AdminAuditLogger;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminUserManagementActivity extends AppCompatActivity {

    private static final String TAG = "AdminUserManagementActivity";

    private EditText etSearchUser;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvCustomers;
    private View layoutEmptyState;

    private UserRepositoryImpl userRepository;
    private final List<User> fullCustomerList = new ArrayList<>();
    private final List<User> filteredCustomerList = new ArrayList<>();
    private CustomerAdapter adapter;

    private String currentSearchQuery = "";
    private int currentFilterId = R.id.chipAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_management);

        userRepository = new UserRepositoryImpl();

        initViews();
        setupListeners();
        loadCustomers();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        etSearchUser = findViewById(R.id.etSearchUser);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        rvCustomers = findViewById(R.id.rvCustomers);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerAdapter(filteredCustomerList);
        rvCustomers.setAdapter(adapter);
    }

    private void setupListeners() {
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim().toLowerCase();
                applyFiltersAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                // If deselected, default back to 'All'
                chipGroupFilters.check(R.id.chipAll);
                currentFilterId = R.id.chipAll;
            } else {
                currentFilterId = checkedId;
            }
            applyFiltersAndSearch();
        });
    }

    private void loadCustomers() {
        userRepository.getAllUsers(new ResultCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                fullCustomerList.clear();
                if (users != null) {
                    for (User u : users) {
                        // Filter for only active (non-deleted) customers
                        if (!u.deleted && "customer".equalsIgnoreCase(u.role)) {
                            fullCustomerList.add(u);
                        }
                    }
                }
                applyFiltersAndSearch();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Lỗi tải khách hàng: " + message);
                Toast.makeText(AdminUserManagementActivity.this, "Lỗi tải dữ liệu khách hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFiltersAndSearch() {
        filteredCustomerList.clear();

        for (User u : fullCustomerList) {
            // Check Search Query (matches name, phone, or email)
            boolean matchesSearch = true;
            if (!currentSearchQuery.isEmpty()) {
                String name = u.name != null ? u.name.toLowerCase() : "";
                String phone = u.phone != null ? u.phone.toLowerCase() : "";
                String email = u.email != null ? u.email.toLowerCase() : "";
                matchesSearch = name.contains(currentSearchQuery) || phone.contains(currentSearchQuery) || email.contains(currentSearchQuery);
            }

            // Check Filter Chip selection
            boolean matchesFilter = true;
            String level = u.memberLevel != null ? u.memberLevel.toLowerCase() : "standard";
            if ("basic".equals(level)) {
                level = "standard";
            }
            String status = u.status != null ? u.status.toLowerCase() : "active";

            if (currentFilterId == R.id.chipStandard) {
                matchesFilter = "standard".equals(level);
            } else if (currentFilterId == R.id.chipVip) {
                matchesFilter = "vip".equals(level);
            } else if (currentFilterId == R.id.chipGold) {
                matchesFilter = "gold".equals(level);
            } else if (currentFilterId == R.id.chipLocked) {
                matchesFilter = "locked".equals(status);
            }

            if (matchesSearch && matchesFilter) {
                filteredCustomerList.add(u);
            }
        }

        adapter.notifyDataSetChanged();

        if (filteredCustomerList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvCustomers.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvCustomers.setVisibility(View.VISIBLE);
        }
    }

    private String getUserDisplayName(User user) {
        if (user.name != null && !user.name.isEmpty()) {
            return user.name;
        }
        if (user.email != null && !user.email.isEmpty()) {
            return user.email;
        }
        if (user.phone != null && !user.phone.isEmpty()) {
            return user.phone;
        }
        return "ID: " + user.uid;
    }

    private void showCustomerDetailsBottomSheet(User user) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_admin_customer_details, null);
        dialog.setContentView(view);

        TextView tvName = view.findViewById(R.id.tvDialogName);
        TextView tvEmail = view.findViewById(R.id.tvDialogEmail);
        TextView tvPhone = view.findViewById(R.id.tvDialogPhone);
        TextView tvGender = view.findViewById(R.id.tvDialogGender);
        TextView tvBirthDate = view.findViewById(R.id.tvDialogBirthDate);
        TextView tvLevel = view.findViewById(R.id.tvDialogLevel);
        TextView tvPoints = view.findViewById(R.id.tvDialogPoints);
        TextView tvJoined = view.findViewById(R.id.tvDialogJoinedDate);
        TextView tvStatus = view.findViewById(R.id.tvDialogStatus);

        Button btnToggleStatus = view.findViewById(R.id.btnDialogToggleStatus);
        Button btnChangeLevel = view.findViewById(R.id.btnDialogChangeLevel);
        Button btnAdjustPoints = view.findViewById(R.id.btnDialogAdjustPoints);
        Button btnDeleteUser = view.findViewById(R.id.btnDialogDeleteUser);
        Button btnGiveVoucher = view.findViewById(R.id.btnDialogGiveVoucher);

        // Populate fields
        tvName.setText(user.name != null ? user.name : "Chưa cập nhật");
        tvEmail.setText(user.email != null ? user.email : "Không có");
        tvPhone.setText(user.phone != null ? user.phone : "Không có");
        tvGender.setText(user.gender != null ? user.gender : "Chưa chọn");
        tvBirthDate.setText(user.birthDate != null ? user.birthDate : "Chưa chọn");
        
        String levelUpper = user.memberLevel != null ? user.memberLevel.toUpperCase(Locale.getDefault()) : "STANDARD";
        tvLevel.setText(levelUpper);
        tvPoints.setText(user.points + " điểm");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        tvJoined.setText(user.createdAt > 0 ? sdf.format(new Date(user.createdAt)) : "Chưa rõ");

        boolean isLocked = "locked".equalsIgnoreCase(user.status);
        if (isLocked) {
            tvStatus.setText("ĐÃ KHÓA");
            tvStatus.setTextColor(Color.RED);
            btnToggleStatus.setText("Mở khóa tài khoản");
            btnToggleStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#10B981"))); // Green
        } else {
            tvStatus.setText("ĐANG HOẠT ĐỘNG");
            tvStatus.setTextColor(Color.parseColor("#10B981"));
            btnToggleStatus.setText("Khóa tài khoản");
            btnToggleStatus.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        }

        // Action: Lock/Unlock
        btnToggleStatus.setOnClickListener(v -> {
            String newStatus = isLocked ? "active" : "locked";
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .update("status", newStatus)
                    .addOnSuccessListener(aVoid -> {
                        user.status = newStatus;
                        applyFiltersAndSearch();
                        dialog.dismiss();
                        Toast.makeText(this, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                        
                        // Log event
                        AdminAuditLogger.log(
                                "UPDATE_CUSTOMER_STATUS",
                                "User",
                                user.uid,
                                (isLocked ? "Mở khóa" : "Khóa") + " tài khoản khách hàng: " + getUserDisplayName(user)
                        );
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Action: Change Level
        btnChangeLevel.setOnClickListener(v -> {
            dialog.dismiss();
            showChangeLevelDialog(user);
        });

        // Action: Adjust Points
        btnAdjustPoints.setOnClickListener(v -> {
            dialog.dismiss();
            showAdjustPointsDialog(user);
        });

        // Action: Delete
        btnDeleteUser.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmDialog(user);
        });

        // Action: Give Voucher
        btnGiveVoucher.setOnClickListener(v -> {
            dialog.dismiss();
            showGiveVoucherDialog(user);
        });

        dialog.show();
    }

    private void showChangeLevelDialog(User user) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_admin_change_level);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        Spinner spinnerLevel = dialog.findViewById(R.id.spinnerMemberLevel);
        Button btnSave = dialog.findViewById(R.id.btnSaveLevel);
        Button btnCancel = dialog.findViewById(R.id.btnCancelLevel);

        String[] levels = {"standard", "vip", "gold"};
        ArrayAdapter<String> adapterLevel = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, levels);
        adapterLevel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(adapterLevel);

        // Select current level
        String currentLevel = user.memberLevel != null ? user.memberLevel.toLowerCase() : "standard";
        for (int i = 0; i < levels.length; i++) {
            if (levels[i].equals(currentLevel)) {
                spinnerLevel.setSelection(i);
                break;
            }
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String selectedLevel = spinnerLevel.getSelectedItem().toString();
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .update("memberLevel", selectedLevel)
                    .addOnSuccessListener(aVoid -> {
                        String oldLevel = user.memberLevel;
                        user.memberLevel = selectedLevel;
                        applyFiltersAndSearch();
                        dialog.dismiss();
                        Toast.makeText(this, "Cập nhật hạng thành công", Toast.LENGTH_SHORT).show();
                        
                        AdminAuditLogger.log(
                                "UPDATE_CUSTOMER_LEVEL",
                                "User",
                                user.uid,
                                "Thay đổi hạng của " + getUserDisplayName(user) + " từ " + oldLevel.toUpperCase() + " sang " + selectedLevel.toUpperCase()
                        );
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private void showAdjustPointsDialog(User user) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_admin_adjust_points);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        TextView tvCurrentPoints = dialog.findViewById(R.id.tvCurrentPointsText);
        EditText etAmount = dialog.findViewById(R.id.etPointsAmount);
        Button btnAdd = dialog.findViewById(R.id.btnPointsAdd);
        Button btnSubtract = dialog.findViewById(R.id.btnPointsSubtract);
        Button btnCancel = dialog.findViewById(R.id.btnCancelPoints);

        tvCurrentPoints.setText("Điểm hiện tại: " + user.points);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnAdd.setOnClickListener(v -> adjustPointsFirebase(user, dialog, etAmount, true));
        btnSubtract.setOnClickListener(v -> adjustPointsFirebase(user, dialog, etAmount, false));

        dialog.show();
    }

    private void adjustPointsFirebase(User user, Dialog dialog, EditText etAmount, boolean isAdd) {
        String inputStr = etAmount.getText().toString().trim();
        if (inputStr.isEmpty()) {
            etAmount.setError("Vui lòng nhập số điểm");
            return;
        }

        int amountVal;
        try {
            amountVal = Integer.parseInt(inputStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Số điểm không hợp lệ");
            return;
        }

        if (amountVal <= 0) {
            etAmount.setError("Số điểm phải lớn hơn 0");
            return;
        }

        int diff = isAdd ? amountVal : -amountVal;
        int finalPoints = user.points + diff;
        if (finalPoints < 0) {
            etAmount.setError("Khách hàng không đủ điểm để trừ");
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .update("points", finalPoints)
                .addOnSuccessListener(aVoid -> {
                    user.points = finalPoints;
                    applyFiltersAndSearch();
                    dialog.dismiss();
                    Toast.makeText(this, "Cập nhật điểm thành công", Toast.LENGTH_SHORT).show();

                    AdminAuditLogger.log(
                            "ADJUST_CUSTOMER_POINTS",
                            "User",
                            user.uid,
                            (isAdd ? "Cộng " : "Trừ ") + amountVal + " điểm cho " + getUserDisplayName(user) + " (Tổng mới: " + finalPoints + ")"
                    );
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmDialog(User user) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_admin_delete_confirm);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        TextView tvTitle = dialog.findViewById(R.id.tvDeleteTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvDeleteMsg);
        Button btnDelete = dialog.findViewById(R.id.btnConfirmDelete);
        Button btnCancel = dialog.findViewById(R.id.btnCancelDelete);

        tvTitle.setText("Xóa khách hàng");
        tvMessage.setText("Bạn có chắc chắn muốn xóa khách hàng " + getUserDisplayName(user) + "? Hành động này sẽ lưu trữ ẩn tài khoản khỏi hệ thống.");

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                    .update("deleted", true)
                    .addOnSuccessListener(aVoid -> {
                        fullCustomerList.remove(user);
                        applyFiltersAndSearch();
                        dialog.dismiss();
                        Toast.makeText(this, "Đã xóa khách hàng thành công", Toast.LENGTH_SHORT).show();

                        AdminAuditLogger.log(
                                "DELETE_CUSTOMER",
                                "User",
                                user.uid,
                                "Xóa tài khoản khách hàng: " + getUserDisplayName(user)
                        );
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    private void showGiveVoucherDialog(User user) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_admin_give_voucher);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(lp);
        }

        TextView tvSubtitle = dialog.findViewById(R.id.tvGiftVoucherSubtitle);
        EditText etDiscount = dialog.findViewById(R.id.etVoucherDiscount);
        EditText etMessage = dialog.findViewById(R.id.etVoucherMessage);
        Button btnSend = dialog.findViewById(R.id.btnSendVoucher);
        Button btnCancel = dialog.findViewById(R.id.btnCancelVoucher);

        tvSubtitle.setText("Khách hàng: " + getUserDisplayName(user));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSend.setOnClickListener(v -> {
            String discountStr = etDiscount.getText().toString().trim();
            if (discountStr.isEmpty()) {
                etDiscount.setError("Vui lòng nhập phần trăm giảm giá");
                return;
            }

            double discountVal;
            try {
                discountVal = Double.parseDouble(discountStr);
                if (discountVal <= 0 || discountVal > 100) {
                    etDiscount.setError("Mức giảm giá không hợp lệ (1-100%)");
                    return;
                }
            } catch (NumberFormatException e) {
                etDiscount.setError("Mức giảm giá không hợp lệ");
                return;
            }

            String message = etMessage.getText().toString().trim();
            if (message.isEmpty()) {
                message = "Bạn được tặng 1 voucher giảm giá " + discountStr + "% từ Admin. Chúc bạn xem phim vui vẻ!";
            }

            sendVoucherToFirebase(user, dialog, discountVal, message);
        });

        dialog.show();
    }

    private void sendVoucherToFirebase(User user, Dialog dialog, double discount, String message) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        com.google.firebase.firestore.WriteBatch batch = db.batch();
        long currentTime = System.currentTimeMillis();

        // 1. Create Voucher
        com.google.firebase.firestore.DocumentReference voucherRef = db.collection("vouchers").document();
        com.example.cinemabooking.domain.model.Voucher voucher = new com.example.cinemabooking.domain.model.Voucher();
        voucher.voucherId = voucherRef.getId();
        voucher.userId = user.uid;
        voucher.voucherType = "ADMIN_GIFT";
        voucher.discountValue = discount;
        voucher.isUsed = false;
        voucher.createdAt = currentTime;
        batch.set(voucherRef, voucher);

        // 2. Create Notification
        com.google.firebase.firestore.DocumentReference notifRef = db.collection("notifications").document();
        com.example.cinemabooking.domain.model.Notification notif = new com.example.cinemabooking.domain.model.Notification();
        notif.notificationId = notifRef.getId();
        notif.userId = user.uid;
        notif.title = "Nhận Voucher từ Admin";
        notif.message = message;
        notif.type = "VOUCHER_RECEIVED";
        notif.isRead = false;
        notif.createdAt = currentTime;
        notif.updatedAt = currentTime;
        batch.set(notifRef, notif);

        // 3. Create AuditLog
        com.google.firebase.firestore.DocumentReference auditRef = db.collection("audit_logs").document();
        com.example.cinemabooking.domain.model.AuditLog auditLog = new com.example.cinemabooking.domain.model.AuditLog();
        auditLog.logId = auditRef.getId();
        auditLog.adminId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null 
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "ADMIN";
        auditLog.action = "GIVE_VOUCHER";
        auditLog.createdAt = currentTime;
        auditLog.actorId = auditLog.adminId;
        auditLog.actorRole = "ADMIN";
        auditLog.targetId = user.uid;
        auditLog.targetType = "USER";
        auditLog.note = "Tặng voucher " + discount + "% cho " + getUserDisplayName(user);
        batch.set(auditRef, auditLog);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Đã tặng Voucher thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi tặng Voucher: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // RecyclerView Adapter
    public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
        private final List<User> items;

        public CustomerAdapter(List<User> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User u = items.get(position);

            holder.tvName.setText(u.name != null ? u.name : "Khách hàng");
            
            String contact = "";
            if (u.phone != null && !u.phone.isEmpty()) {
                contact += u.phone;
            }
            if (u.email != null && !u.email.isEmpty()) {
                if (!contact.isEmpty()) contact += " • ";
                contact += u.email;
            }
            holder.tvContact.setText(contact.isEmpty() ? "Không có thông tin liên hệ" : contact);

            // Level styling
            String level = u.memberLevel != null ? u.memberLevel.toUpperCase(Locale.getDefault()) : "STANDARD";
            if ("BASIC".equals(level)) {
                level = "STANDARD";
            }
            holder.tvLevel.setText(level);
            if ("GOLD".equals(level)) {
                holder.tvLevel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF3CD")));
                holder.tvLevel.setTextColor(Color.parseColor("#856404"));
            } else if ("VIP".equals(level)) {
                holder.tvLevel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F8D7DA")));
                holder.tvLevel.setTextColor(Color.parseColor("#721C24"));
            } else {
                holder.tvLevel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F6F4F8")));
                holder.tvLevel.setTextColor(Color.parseColor("#4A4650"));
            }

            // Points
            holder.tvPoints.setText(u.points + " điểm");

            // Status light indicator
            boolean isActive = !"locked".equalsIgnoreCase(u.status);
            if (isActive) {
                holder.viewStatus.setBackgroundResource(com.example.cinemabooking.R.drawable.dot_active);
            } else {
                holder.viewStatus.setBackground(new ColorDrawable(Color.RED));
                // Make red dot round using custom programmatic rounded background
                holder.viewStatus.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
                holder.viewStatus.setClipToOutline(true);
            }

            holder.itemView.setOnClickListener(v -> showCustomerDetailsBottomSheet(u));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvContact, tvLevel, tvPoints;
            View viewStatus;
            ImageView imgAvatar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvCustomerName);
                tvContact = itemView.findViewById(R.id.tvCustomerContact);
                tvLevel = itemView.findViewById(R.id.tvCustomerLevel);
                tvPoints = itemView.findViewById(R.id.tvCustomerPoints);
                viewStatus = itemView.findViewById(R.id.viewStatusIndicator);
                imgAvatar = itemView.findViewById(R.id.imgAvatar);
            }
        }
    }
}