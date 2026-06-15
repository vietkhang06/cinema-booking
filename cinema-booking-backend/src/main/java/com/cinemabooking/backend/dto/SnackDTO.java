/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend DTO
 * 
 * Mô tả: 
 * Đối tượng truyền tải dữ liệu cho các sản phẩm Đồ ăn/Thức uống (Snack).
 * Trả về thông tin sản phẩm và giá cả cho client hiển thị.
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
public class SnackDTO {
    public static final String COLLECTION_NAME = "snacks";

    public String snackId;
    public String categoryId;
    public String name;
    public String description;
    public double price;
    public String imageUrl;
    public boolean isAvailable;
    public String status;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;

}
