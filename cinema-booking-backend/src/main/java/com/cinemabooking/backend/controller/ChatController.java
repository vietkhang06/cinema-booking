package com.cinemabooking.backend.controller;

import lombok.extern.slf4j.Slf4j;

import com.cinemabooking.backend.dto.ApiResponse;
import com.cinemabooking.backend.dto.ChatMessage;
import com.cinemabooking.backend.dto.Conversation;
import com.cinemabooking.backend.dto.UserDTO;
import com.cinemabooking.backend.dto.request.SendMessageRequest;
import com.cinemabooking.backend.service.ChatService;
import com.cinemabooking.backend.service.ConversationService;
import com.cinemabooking.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Autowired private ChatService chatService;
    @Autowired private ConversationService conversationService;
    @Autowired private UserService userService;
    @Autowired private com.google.cloud.firestore.Firestore firestore;

    /**
     * POST /api/v1/chat/messages
     * Send a message.
     */
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<ChatMessage>> sendMessage(
            @AuthenticationPrincipal String userId,
            @RequestBody SendMessageRequest req
    ) throws ExecutionException, InterruptedException {
        ChatMessage message = chatService.sendMessage(userId, req);
        return ResponseEntity.ok(
                ApiResponse.<ChatMessage>builder()
                    .success(true)
                    .data(message)
                    .build()
        );
    }

    /**
     * GET /api/v1/chat/conversations/{convoId}/messages?limit=20
     */
    @GetMapping("/conversations/{convoId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getMessages(
            @AuthenticationPrincipal String userId,
            @PathVariable String convoId,
            @RequestParam(defaultValue = "20") int limit
    ) throws ExecutionException, InterruptedException {
        if (userId == null) {
            return ResponseEntity.status(401).body(
                    ApiResponse.<List<ChatMessage>>builder()
                            .success(false)
                            .message("Vui lòng đăng nhập.")
                            .build()
            );
        }

        Conversation convo = conversationService.getConversationById(convoId);
        if (convo == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isParticipant = convo.getParticipantIds() != null && convo.getParticipantIds().contains(userId);
        boolean isStaffOrAdmin = isUserActiveStaffOrAdmin(userId);

        if (!isParticipant && !isStaffOrAdmin) {
            return ResponseEntity.status(403).body(
                    ApiResponse.<List<ChatMessage>>builder()
                            .success(false)
                            .message("Bạn không có quyền truy cập cuộc hội thoại này.")
                            .build()
            );
        }

        return ResponseEntity.ok(
                ApiResponse.<List<ChatMessage>>builder()
                        .success(true)
                        .data(chatService.getMessages(convoId, limit, null))
                        .build()
        );
    }

    /**
     * POST /api/v1/chat/conversations/{convoId}/read
     */
    @PostMapping("/conversations/{convoId}/read")
    public ResponseEntity<Void> readConversation(
            @AuthenticationPrincipal String userId,
            @PathVariable String convoId
    ) throws ExecutionException, InterruptedException {
        conversationService.markAsRead(convoId, userId, System.currentTimeMillis());
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/chat/support/init
     */
    @PostMapping("/support/init")
    public ResponseEntity<ApiResponse<Conversation>> initSupportConversation(
            @AuthenticationPrincipal String userId
    ) throws ExecutionException, InterruptedException {
        Conversation convo = conversationService.getConversationByUserIds(userId, "SUPPORT_BOT");
        if (convo == null) {
            convo = conversationService.createSupportConversation(userId, System.currentTimeMillis());
        } else {
            if (!"BOT_ONLY".equals(convo.getStatus())) {
                convo.setStatus("BOT_ONLY");
                convo.setAssignedStaffId(null);
                firestore.collection(Conversation.COLLECTION_NAME).document(convo.getConvoId())
                        .update("status", "BOT_ONLY", "assignedStaffId", null).get();
            }
        }

        if (convo != null) {
            log.info("SUPPORT_INIT_STATUS={}", convo.getStatus());
            log.info("SUPPORT_INIT_CONVO={}", convo.getConvoId());
        }

        return ResponseEntity.ok(
                ApiResponse.<Conversation>builder()
                        .success(true)
                        .data(convo)
                        .build()
        );
    }

    private boolean isUserActiveStaffOrAdmin(String userId) throws ExecutionException, InterruptedException {
        UserDTO user = userService.getUserById(userId);
        if (user == null) return false;
        return ("staff".equalsIgnoreCase(user.getRole()) || "admin".equalsIgnoreCase(user.getRole()))
                && !"inactive".equalsIgnoreCase(user.getStatus())
                && !Boolean.TRUE.equals(user.getDeleted());
    }

    /**
     * DELETE /api/v1/chat/conversations/{convoId}/messages
     */
    @DeleteMapping("/conversations/{convoId}/messages")
    public ResponseEntity<ApiResponse<Void>> clearConversationMessages(
            @AuthenticationPrincipal String userId,
            @PathVariable String convoId
    ) throws ExecutionException, InterruptedException {
        Conversation convo = conversationService.getConversationById(convoId);
        if (convo == null) {
            return ResponseEntity.notFound().build();
        }

        boolean isParticipant = convo.getParticipantIds().contains(userId);
        boolean isStaffOrAdmin = isUserActiveStaffOrAdmin(userId);

        if (!isParticipant && !isStaffOrAdmin) {
            return ResponseEntity.status(403).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("Bạn không có quyền xóa hội thoại này.")
                            .build()
            );
        }

        chatService.clearConversationMessages(convoId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Đã xóa toàn bộ tin nhắn thành công.")
                .build());
    }
}
