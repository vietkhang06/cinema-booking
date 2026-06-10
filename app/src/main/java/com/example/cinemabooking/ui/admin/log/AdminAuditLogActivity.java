package com.example.cinemabooking.ui.admin.log;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.dto.AuditLogDTO;
import com.example.cinemabooking.ui.admin.AdminBottomNavHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminAuditLogActivity extends AppCompatActivity {

    private RecyclerView auditLogsRv;
    private TextView tvNoLogs;
    private LogAdapter adapter;
    private final List<AuditLogDTO> logList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_audit_log);

        AdminBottomNavHelper.setupAdminBottomNavigation(this, 3);

        initViews();
        loadLogs();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        auditLogsRv = findViewById(R.id.audit_logs_rv);
        tvNoLogs = findViewById(R.id.tv_no_logs);

        auditLogsRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LogAdapter(logList);
        auditLogsRv.setAdapter(adapter);
    }

    private void loadLogs() {
        FirebaseFirestore.getInstance().collection("audit_logs")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(150)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    logList.clear();
                    if (queryDocumentSnapshots != null) {
                        logList.addAll(queryDocumentSnapshots.toObjects(AuditLogDTO.class));
                    }
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
                    Toast.makeText(this, "Lỗi tải nhật ký: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

            String actionStr = log.action != null ? log.action.toUpperCase(Locale.getDefault()) : "ACTION";
            holder.tvActionBadge.setText(actionStr);
            holder.tvTimestamp.setText(dateFormat.format(new Date(log.createdAt)));
            holder.tvNote.setText(log.note);
            holder.tvActor.setText("Thực hiện bởi: " + log.actorId + " (" + log.actorRole + ")");

            // Dynamically color code badges for clear visual organization
            if (actionStr.startsWith("CREATE_") || actionStr.contains("CONFIRM") || actionStr.contains("PAYMENT") || actionStr.contains("CHECKIN")) {
                // Positive action - Green
                holder.tvActionBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2E7D32")));
            } else if (actionStr.startsWith("UPDATE_")) {
                // Modification - Blue
                holder.tvActionBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1565C0")));
            } else if (actionStr.startsWith("DELETE_") || actionStr.contains("RELEASE") || actionStr.contains("FREE")) {
                // Destructive/Cancellation - Red/Orange
                holder.tvActionBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#C62828")));
            } else {
                // Secondary / default - Dark Slate
                holder.tvActionBadge.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#4A4650")));
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