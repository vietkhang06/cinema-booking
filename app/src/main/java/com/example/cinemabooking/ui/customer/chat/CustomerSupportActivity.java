package com.example.cinemabooking.ui.customer.chat;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.request.SendMessageRequest;
import com.example.cinemabooking.data.remote.api.ChatApiService;
import com.example.cinemabooking.data.remote.api.RetrofitClient;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.domain.model.ChatMessage;
import com.example.cinemabooking.domain.model.Conversation;
import com.example.cinemabooking.domain.model.User;
import com.example.cinemabooking.ui.customer.chat.adapter.SupportMessageAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerSupportActivity extends AppCompatActivity {

    private static final String TAG = "CustomerSupportActivity";

    // Views
    private RecyclerView recyclerViewMessages;
    private EditText etMessage;
    private ImageButton btnBack;
    private ImageButton btnSend;
    private ImageButton btnClearChat;
    private TextView tvSupportName;
    private TextView tvOnlineStatus;
    private ImageView ivAvatar;
    private LinearLayout statusBanner;
    private TextView tvStatusText;
    private View quickActionsScroll;
    private View layoutResetBot;
    private com.google.android.material.button.MaterialButton btnResetBot;

    // Adapters & Data
    private SupportMessageAdapter supportMessageAdapter;
    private final List<ChatMessage> messages = new ArrayList<>();
    private final ChatApiService chatApiService = RetrofitClient.getInstance().create(ChatApiService.class);
    private String authUserId;
    private Conversation conversation;
    private ListenerRegistration messageListener;
    private ListenerRegistration convoListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_customer_support);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        User cachedProfile = ServiceProvider.getInstance().getProfileService().getCachedProfile();
        if (cachedProfile != null) {
            authUserId = cachedProfile.uid;
        } else {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                authUserId = firebaseUser.getUid();
            } else {
                Toast.makeText(this, "Bạn cần đăng nhập tài khoản để tiếp tục.", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        bindViews();
        setupRecyclerView();
        setupInputBar();
        setupFaqChips();

        btnBack.setOnClickListener(v -> finish());
        btnClearChat.setOnClickListener(v -> confirmClearChat());
        btnResetBot.setOnClickListener(v -> resetConversationToBot());

        loadSupportConversation();
    }

    private void bindViews() {
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        btnClearChat = findViewById(R.id.btnClearChat);
        tvSupportName = findViewById(R.id.tvSupportName);
        tvOnlineStatus = findViewById(R.id.tvOnlineStatus);
        ivAvatar = findViewById(R.id.ivAvatar);
        statusBanner = findViewById(R.id.statusBanner);
        tvStatusText = findViewById(R.id.tvStatusText);
        quickActionsScroll = findViewById(R.id.quickActionsScroll);
        layoutResetBot = findViewById(R.id.layoutResetBot);
        btnResetBot = findViewById(R.id.btnResetBot);
    }

    private void setupRecyclerView() {
        supportMessageAdapter = new SupportMessageAdapter(authUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(supportMessageAdapter);
    }

    private void setupInputBar() {
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
            if (!text.isEmpty()) {
                sendMessage(text);
            }
        });
    }

    private void setupFaqChips() {
        findViewById(R.id.chipMovies).setOnClickListener(v -> sendMessage("phim đang chiếu"));
        findViewById(R.id.chipShowtimes).setOnClickListener(v -> sendMessage("suất chiếu hôm nay"));
        findViewById(R.id.chipMyBooking).setOnClickListener(v -> sendMessage("vé của tôi"));
        findViewById(R.id.chipMyVouchers).setOnClickListener(v -> sendMessage("voucher của tôi"));
    }

    private void loadSupportConversation() {
        chatApiService.initSupportConversation().enqueue(new Callback<ApiResponse<Conversation>>() {
            @Override
            public void onResponse(Call<ApiResponse<Conversation>> call, Response<ApiResponse<Conversation>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    conversation = response.body().getData();
                    if (conversation != null) {
                        Log.d(TAG, "INIT_RESPONSE_CONVO_ID=" + conversation.convoId);
                    }
                    updateConversationUI();
                    addRealtimeMessageListener(conversation.convoId);
                    markAsRead(conversation.convoId);
                } else {
                    Toast.makeText(CustomerSupportActivity.this, "Không thể tải cuộc hội thoại hỗ trợ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Conversation>> call, Throwable t) {
                Toast.makeText(CustomerSupportActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateConversationUI() {
        if (conversation == null) return;

        // Visual layout updates (Forces bot-only presentation)
        tvSupportName.setText("Trợ lý ảo Cinema");
        tvOnlineStatus.setText("Trực tuyến");
        tvOnlineStatus.setTextColor(Color.parseColor("#4CAF50"));
        ivAvatar.setImageResource(R.drawable.user_solid_full); 
        statusBanner.setBackgroundColor(Color.parseColor("#E8EAF6"));
        tvStatusText.setText("🤖 Trợ lý ảo (Bot) đang hỗ trợ");
        tvStatusText.setTextColor(Color.parseColor("#3F51B5"));
        quickActionsScroll.setVisibility(View.VISIBLE);
        findViewById(R.id.inputBar).setVisibility(View.VISIBLE);
        layoutResetBot.setVisibility(View.GONE);
    }

    private void sendMessage(String text) {
        if (text == null || text.trim().isEmpty() || conversation == null) return;

        SendMessageRequest req = new SendMessageRequest();
        req.content = text;
        req.receiverId = "SUPPORT_BOT"; // Always route to Virtual Assistant

        chatApiService.sendMessage(req).enqueue(new Callback<ApiResponse<ChatMessage>>() {
            @Override
            public void onResponse(Call<ApiResponse<ChatMessage>> call, Response<ApiResponse<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    etMessage.setText("");
                } else {
                    Toast.makeText(CustomerSupportActivity.this, "Không thể gửi tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ChatMessage>> call, Throwable t) {
                Toast.makeText(CustomerSupportActivity.this, "Lỗi gửi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resetConversationToBot() {
        // Automatically handled since it is bot-only. Just load support convo.
        loadSupportConversation();
    }

    private void confirmClearChat() {
        if (conversation == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xoá toàn bộ hội thoại này không?")
                .setPositiveButton("Xóa", (dialog, which) -> clearChatMessages())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void clearChatMessages() {
        chatApiService.clearConversationMessages(conversation.convoId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(CustomerSupportActivity.this, "Đã xóa toàn bộ tin nhắn", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CustomerSupportActivity.this, "Không thể xóa tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(CustomerSupportActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addRealtimeMessageListener(String convoId) {
        if (messageListener != null) return;

        messageListener = FirebaseFirestore.getInstance().collection("chat_messages")
                .whereEqualTo("convoId", convoId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Firestore message listener error: " + error.getMessage());
                        return;
                    }

                    if (snapshot != null) {
                        List<ChatMessage> allMessages = snapshot.getDocuments().stream()
                                .map(doc -> doc.toObject(ChatMessage.class))
                                .filter(java.util.Objects::nonNull)
                                .sorted((m1, m2) -> Long.compare(m1.sentAt, m2.sentAt))
                                .collect(Collectors.toList());

                        messages.clear();
                        messages.addAll(allMessages);
                        supportMessageAdapter.submitList(new ArrayList<>(messages));
                        recyclerViewMessages.scrollToPosition(supportMessageAdapter.getItemCount() - 1);

                        if (!messages.isEmpty()) {
                            ChatMessage lastMsg = messages.get(messages.size() - 1);
                            if (lastMsg.senderId != null && !lastMsg.senderId.equals(authUserId)) {
                                markAsRead(convoId);
                            }
                        }
                    }
                });

        convoListener = FirebaseFirestore.getInstance().collection("conversations").document(convoId)
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot != null && snapshot.exists()) {
                        Conversation convo = snapshot.toObject(Conversation.class);
                        if (convo != null) {
                            conversation = convo;
                            updateConversationUI();
                        }
                    }
                });
    }

    private void markAsRead(String convoId) {
        chatApiService.markAsRead(convoId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {}
            @Override public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void removeRealtimeListener() {
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
        if (convoListener != null) {
            convoListener.remove();
            convoListener = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeRealtimeListener();
    }
}
