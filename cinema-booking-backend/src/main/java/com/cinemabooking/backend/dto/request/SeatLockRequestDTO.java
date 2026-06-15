/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend Request DTO
 * 
 * Mô tả: 
 * Đối tượng gửi từ Client lên Server để yêu cầu khoá ghế tạm thời (Hold seat)
 * trong lúc người dùng thực hiện các bước thanh toán, tránh người khác chọn trùng.
 */
package com.cinemabooking.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatLockRequestDTO {
    private String showtimeId;
    private List<String> seatIds;
}
