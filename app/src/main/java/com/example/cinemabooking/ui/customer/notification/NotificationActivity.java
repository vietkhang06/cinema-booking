package com.example.cinemabooking.ui.customer.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.repository.NotificationRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotification;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextView btnMarkAllRead;
    private NotificationAdapter adapter;
    private ListenerRegistration listenerRegistration;
    private List<Notification> currentNotifications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bindActions();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvNotification = findViewById(R.id.rv_notifications);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read);

        rvNotification.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new NotificationAdapter(currentNotifications, notification -> {
            if (!notification.isRead) {
                // Immediate UX feedback
                notification.isRead = true;
                adapter.notifyDataSetChanged();
                checkUnreadStatus();
                // API call
                new NotificationRepositoryImpl().markAsRead(notification.notificationId, null);
            }
        });
        rvNotification.setAdapter(adapter);

        btnMarkAllRead.setOnClickListener(v -> {
            NotificationRepositoryImpl repo = new NotificationRepositoryImpl();
            for (Notification notification : currentNotifications) {
                if (!notification.isRead) {
                    notification.isRead = true;
                    repo.markAsRead(notification.notificationId, null);
                }
            }
            adapter.notifyDataSetChanged();
            checkUnreadStatus();
        });
    }

    private void checkUnreadStatus() {
        boolean hasUnread = false;
        for (Notification n : currentNotifications) {
            if (!n.isRead) {
                hasUnread = true;
                break;
            }
        }
        btnMarkAllRead.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
    }

    private void bindActions() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        progressBar.setVisibility(View.VISIBLE);
        
        listenerRegistration = new NotificationRepositoryImpl().listenToUserNotifications(userId, new ResultCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> result) {
                progressBar.setVisibility(View.GONE);
                if (result == null || result.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvNotification.setVisibility(View.GONE);
                    btnMarkAllRead.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    rvNotification.setVisibility(View.VISIBLE);
                    currentNotifications.clear();
                    currentNotifications.addAll(result);
                    adapter.setNotifications(currentNotifications);
                    checkUnreadStatus();
                }
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
