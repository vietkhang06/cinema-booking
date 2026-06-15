/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend Payment
 * 
 * Mô tả: 
 * Trạng thái kết hợp giữa đơn đặt vé (Booking) và giao dịch thanh toán tương ứng.
 */
package com.cinemabooking.backend.payment.model;

public enum BookingPaymentState {
    PENDING,
    PAID,
    CANCELLED
}
