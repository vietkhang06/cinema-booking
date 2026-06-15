/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend DTO
 * 
 * Mô tả: 
 * Đối tượng truyền tải dữ liệu đại diện cho Người dùng (User).
 * Dùng để trả về thông tin hồ sơ người dùng một cách an toàn mà không làm lộ dữ liệu nhạy cảm.
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
public class UserDTO {
    public static final String COLLECTION_NAME = "users";
    
    private String uid;
    private String email;
    private String phone;
    private String name;
    private String birthDate;
    private String gender;
    private String avatarUrl;
    private String role;
    private String status;
    private String memberLevel;
    private Long createdAt;
    private Long updatedAt;
    private Boolean deleted;
    private Integer points;
}
