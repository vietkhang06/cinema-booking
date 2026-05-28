package com.example.cinemabooking.ui.staff;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.AuthActivity;
import com.example.cinemabooking.data.dto.AuditLogDTO;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StaffAuditLogActivity extends AuthActivity {

    private RecyclerView auditLogsRv;
    private TextView tvNoLogs;
    private View backBtn;
    private LogAdapter adapter;
    private final List<AuditLogDTO> logList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_audit_log);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bindActions();
        loadLogs();
    }

    private void initViews() {
        auditLogsRv = findViewById(R.id.audit_logs_rv);
        tvNoLogs = findViewById(R.id.tv_no_logs);
        backBtn = findViewById(R.id.back_btn);

        auditLogsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LogAdapter(logList);
        auditLogsRv.setAdapter(adapter);
    }

    private void bindActions() {
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadLogs() {
        showLoading(true);
        FirebaseFirestore.getInstance().collection("audit_logs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showLoading(false);
                    logList.clear();
                    logList.addAll(queryDocumentSnapshots.toObjects(AuditLogDTO.class));
                    adapter.notifyDataSetChanged();

                    if (logList.isEmpty()) {
                        tvNoLogs.setVisibility(View.VISIBLE);
                        auditLogsRv.setVisibility(View.GONE);
                    } else {
                        tvNoLogs.setVisibility(View.GONE);
                        auditLogsRv.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showToast("Lỗi tải nhật ký: " + e.getMessage());
                });
    }

    private class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
        private final List<AuditLogDTO> items;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

        public LogAdapter(List<AuditLogDTO> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_audit_log_card, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AuditLogDTO log = items.get(position);

            holder.tvActionBadge.setText(log.action != null ? log.action.toUpperCase(Locale.getDefault()) : "ACTION");
            holder.tvTimestamp.setText(dateFormat.format(new Date(log.createdAt)));
            holder.tvNote.setText(log.note);
            holder.tvActor.setText("Thực hiện bởi: " + log.actorId + " (" + log.actorRole + ")");

            // Badge color based on action type
            String action = log.action != null ? log.action.toUpperCase(Locale.getDefault()) : "";
            if (action.contains("CHECKIN")) {
                holder.tvActionBadge.setBackgroundColor(0xFF2196F3); // Blue
            } else if (action.contains("PAYMENT") || action.contains("CONFIRM")) {
                holder.tvActionBadge.setBackgroundColor(0xFF4CAF50); // Green
            } else if (action.contains("RELEASE") || action.contains("FREE")) {
                holder.tvActionBadge.setBackgroundColor(0xFFFF9800); // Orange
            } else {
                holder.tvActionBadge.setBackgroundColor(0xFF607D8B); // Slate Gray
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvActionBadge, tvTimestamp, tvNote, tvActor;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvActionBadge = itemView.findViewById(R.id.tv_action_badge);
                tvTimestamp = itemView.findViewById(R.id.tv_timestamp);
                tvNote = itemView.findViewById(R.id.tv_note);
                tvActor = itemView.findViewById(R.id.tv_actor);
            }
        }
    }
}
