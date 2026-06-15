/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend Request DTO
 * 
 * Mô tả: 
 * DTO đại diện cho một request gửi tin nhắn trong hệ thống Chat nội bộ hoặc Hỗ trợ khách hàng.
 */
package com.cinemabooking.backend.dto.request;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String receiverId;
    private String content;
    private String imgUrl;
}
