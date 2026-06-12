package com.example.cinemabooking.domain.model;

public class ChatMessage {
    public String messageId;
    public String convoId;
    public String senderId;
    public String receiverId;
    public String content;
    public String type;
    public String imgUrl;
    public Long sentAt;

    public ChatMessage() {
    }
}