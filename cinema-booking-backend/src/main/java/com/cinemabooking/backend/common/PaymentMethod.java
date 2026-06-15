/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend Common
 * 
 * Mô tả: 
 * Enum dùng chung toàn hệ thống định nghĩa các hình thức thanh toán được hỗ trợ.
 */
package com.cinemabooking.backend.common;

public enum PaymentMethod {
    cash,
    credit_card,
    momo,
    bank,
    bank_transfer
}
