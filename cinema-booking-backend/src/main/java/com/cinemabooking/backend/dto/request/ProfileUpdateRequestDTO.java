/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend Request DTO
 * 
 * Mô tả: 
 * Đối tượng Request chứa dữ liệu từ Client gửi lên khi người dùng
 * thực hiện cập nhật thông tin hồ sơ cá nhân.
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
public class ProfileUpdateRequestDTO {
    String username;
    String phone;
    String avatarUrl;
}