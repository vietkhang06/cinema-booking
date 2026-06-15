/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend DTO
 * 
 * Mô tả: 
 * Dữ liệu chuyển giao mô tả một tin nhắn đơn lẻ trong hội thoại Chat.
 */
package com.cinemabooking.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    public static final String COLLECTION_NAME = "chat_messages";

    String messageId;
    String convoId;
    String senderId;
    String receiverId;

    String content;
    String imgUrl;
    Long sentAt;
}
