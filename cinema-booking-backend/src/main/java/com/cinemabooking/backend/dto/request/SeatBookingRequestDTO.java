/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend Request DTO
 * 
 * Mô tả: 
 * Đối tượng payload khi người dùng xác nhận đặt ghế (sau khi đã khoá ghế thành công).
 * Chứa danh sách các ghế đã chọn và mã đơn hàng liên quan.
 */
package com.cinemabooking.backend.dto.request;

import com.cinemabooking.backend.common.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatBookingRequestDTO {
    private String showtimeId;
    private List<String> seatIds;
    private List<SnackOrder> snackOrders;

    private PaymentMethod paymentMethod;
    private String promoCode;
    private Boolean useStars;

    public record SnackOrder(String snackId, int quantity) { }
}
