/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend Request DTO
 * 
 * Mô tả: 
 * Đối tượng payload chứa yêu cầu đặt hàng đồ ăn vặt (Bắp/Nước) đi kèm với vé.
 */
package com.cinemabooking.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CineShopOrderRequestDTO {
    private String itemName;
    private String itemImageUrl;
    private Integer quantity;
    private Double totalPrice;
    private String paymentMethod;
}
