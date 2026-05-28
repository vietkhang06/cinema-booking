package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.ChatMessage;
import com.example.cinemabooking.domain.model.ChatThread;

import java.util.List;

public interface ChatRepository {
    void createThread(ChatThread thread, ResultCallback<ChatThread> callback);
    void getThreadById(String threadId, ResultCallback<ChatThread> callback);
    void getThreadByUserId(String userId, ResultCallback<ChatThread> callback);
    void getMessagesByThreadId(String threadId, ResultCallback<List<ChatMessage>> callback);
    void sendMessage(ChatMessage message, ResultCallback<ChatMessage> callback);
    void markMessagesAsRead(String threadId, String receiverId, ResultCallback<Void> callback);
    void closeThread(String threadId, ResultCallback<ChatThread> callback);
}