/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend Payment
 * 
 * Mô tả: 
 * Entity mô tả một giao dịch thanh toán cụ thể, bao gồm số tiền, phương thức, và thời gian.
 */
package com.cinemabooking.backend.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private String paymentId;
    private String bookingId;
    private String paymentCode;
    private String userId;
    private String provider;
    private double amount;
    private String status;
    private String transactionId;
    private String payUrl;
    private long createdAt;
    private long updatedAt;
}
