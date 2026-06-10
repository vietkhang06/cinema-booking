package com.example.cinemabooking.ui.admin.notification;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.repository.UserRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.User;
import com.example.cinemabooking.ui.admin.log.AdminAuditLogger;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminSendNotificationActivity extends AppCompatActivity {

    private static final int REQUEST_SELECT_USERS = 1001;

    private EditText edtNotifTitle, edtNotifMessage;
    private TextView tvRecipientCount;
    private RecyclerView rvRecipients;

    private ArrayList<String> selectedUids = new ArrayList<>();
    private List<User> allUsers = new ArrayList<>();
    private List<User> selectedUsers = new ArrayList<>();
    
    private SelectedRecipientAdapter adapter;
    private UserRepositoryImpl userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_send_notification);

        userRepository = new UserRepositoryImpl();

        initViews();
        setupListeners();
        loadAllCustomers();
    }

    private void initViews() {
        edtNotifTitle = findViewById(R.id.edtNotifTitle);
        edtNotifMessage = findViewById(R.id.edtNotifMessage);
        tvRecipientCount = findViewById(R.id.tvRecipientCount);
        rvRecipients = findViewById(R.id.rvRecipients);

        rvRecipients.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectedRecipientAdapter(selectedUsers);
        rvRecipients.setAdapter(adapter);

        updateRecipientCount();
    }

    private void loadAllCustomers() {
        userRepository.getAllUsers(new ResultCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                allUsers.clear();
                if (users != null) {
                    for (User u : users) {
                        if (!u.deleted && "customer".equalsIgnoreCase(u.role)) {
                            allUsers.add(u);
                        }
                    }
                }
            }

            @Override
            public void onError(String message) {
                Log.e("AdminSendNotif", "Lỗi tải khách hàng: " + message);
            }
        });
    }

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnAddRecipients).setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminSelectUserActivity.class);
            intent.putStringArrayListExtra("SELECTED_UIDS", selectedUids);
            startActivityForResult(intent, REQUEST_SELECT_USERS);
        });

        findViewById(R.id.btnSendNotification).setOnClickListener(v -> {
            String title = edtNotifTitle.getText().toString().trim();
            String message = edtNotifMessage.getText().toString().trim();

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ tiêu đề và nội dung", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedUids.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 người nhận", Toast.LENGTH_SHORT).show();
                return;
            }

            showConfirmDialog(title, message);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_USERS && resultCode == RESULT_OK && data != null) {
            ArrayList<String> uids = data.getStringArrayListExtra("SELECTED_UIDS");

            if (uids != null) {
                selectedUids.clear();
                selectedUids.addAll(uids);

                selectedUsers.clear();
                for (User u : allUsers) {
                    if (selectedUids.contains(u.uid)) {
                        selectedUsers.add(u);
                    }
                }

                adapter.notifyDataSetChanged();
                updateRecipientCount();
            }
        }
    }

    private void updateRecipientCount() {
        tvRecipientCount.setText("Đã chọn: " + selectedUids.size() + " khách hàng");
    }

    private void showConfirmDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận gửi thông báo")
                .setMessage("Bạn có chắc chắn muốn gửi thông báo này cho " + selectedUids.size() + " khách hàng?")
                .setPositiveButton("GỬI", (dialog, which) -> sendNotificationToFirestore(title, message))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void sendNotificationToFirestore(String title, String message) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang gửi thông báo...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        long timestamp = System.currentTimeMillis();

        int count = 0;
        ArrayList<WriteBatch> batches = new ArrayList<>();
        batches.add(batch);
        int batchIndex = 0;

        for (String uid : selectedUids) {
            DocumentReference notifRef = db.collection("notifications").document();
            
            com.example.cinemabooking.domain.model.Notification notif = new com.example.cinemabooking.domain.model.Notification();
            notif.notificationId = notifRef.getId();
            notif.userId = uid;
            notif.title = title;
            notif.message = message;
            notif.type = "ADMIN_MESSAGE";
            notif.isRead = false;
            notif.createdAt = timestamp;
            notif.updatedAt = timestamp;

            batches.get(batchIndex).set(notifRef, notif);
            count++;

            // Split into multiple batches if > 400
            if (count % 400 == 0) {
                batches.add(db.batch());
                batchIndex++;
            }
        }

        executeBatches(batches, 0, progressDialog);
    }

    private void executeBatches(ArrayList<WriteBatch> batches, int index, ProgressDialog progressDialog) {
        if (index >= batches.size()) {
            progressDialog.dismiss();
            Toast.makeText(this, "Gửi thông báo thành công!", Toast.LENGTH_SHORT).show();
            AdminAuditLogger.log(
                    "SEND_GLOBAL_NOTIFICATION",
                    "Notification",
                    "admin",
                    "Đã gửi thông báo cho " + selectedUids.size() + " khách hàng. Tiêu đề: " + edtNotifTitle.getText().toString()
            );
            finish();
            return;
        }

        batches.get(index).commit()
                .addOnSuccessListener(aVoid -> executeBatches(batches, index + 1, progressDialog))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi gửi thông báo: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Inner Adapter for Recipient List
    private class SelectedRecipientAdapter extends RecyclerView.Adapter<SelectedRecipientAdapter.ViewHolder> {
        private final List<User> items;

        public SelectedRecipientAdapter(List<User> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user_selectable, parent, false);
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

            // Hide the checkbox as we only want to display user info
            holder.cbSelect.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvContact, tvLevel, tvPoints;
            CheckBox cbSelect;
            ImageView imgAvatar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvCustomerName);
                tvContact = itemView.findViewById(R.id.tvCustomerContact);
                tvLevel = itemView.findViewById(R.id.tvCustomerLevel);
                tvPoints = itemView.findViewById(R.id.tvCustomerPoints);
                cbSelect = itemView.findViewById(R.id.cbSelect);
                imgAvatar = itemView.findViewById(R.id.imgAvatar);
            }
        }
    }
}
