/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend DTO
 * 
 * Mô tả: 
 * Đối tượng truyền tải dữ liệu (Data Transfer Object) lưu trữ bản tóm tắt
 * của đơn đặt đồ ăn vặt (Snack). Dùng để hiển thị hoặc lưu trữ lịch sử giao dịch.
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
public class SnackOrderSnapshot {
    private String snackId;
    private String snackName;
    private String snackImgURL;
    private double price;
    private int quantity;
}