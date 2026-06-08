package com.example.cinemabooking.data.remote.api;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.request.SendMessageRequest;
import com.example.cinemabooking.domain.model.ChatMessage;
import com.example.cinemabooking.domain.model.Conversation;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatApiService {
    @POST("chat/messages")
    Call<ApiResponse<ChatMessage>> sendMessage(@Body SendMessageRequest message);

    @POST("chat/conversations/{convoId}/read")
    Call<Void> markAsRead(@Path("convoId") String convoId);

    @POST("chat/support/init")
    Call<ApiResponse<Conversation>> initSupportConversation();

    @DELETE("chat/conversations/{convoId}/messages")
    Call<ApiResponse<Void>> clearConversationMessages(@Path("convoId") String convoId);
}
