/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend Payment
 * 
 * Mô tả: 
 * Enum định nghĩa các trạng thái của một giao dịch thanh toán (Thành công, Thất bại, Đang chờ).
 */
package com.cinemabooking.backend.payment.model;

public enum PaymentStatus {
    PENDING,
    WAITING_CONFIRMATION,
    PAID,
    FAILED,
    EXPIRED,
    CANCELLED
}

