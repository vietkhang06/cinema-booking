/**
 * Dự án: Hệ thống Đặt vé Xem phim (Cinema Booking System)
 * Phân hệ: Backend DTO
 * 
 * Mô tả: 
 * Đối tượng mô tả dữ liệu Phòng chiếu (Room).
 * Quản lý thông tin về cấu trúc ghế ngồi và số lượng ghế trong một phòng của rạp.
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
public class RoomDTO {
    public static final String COLLECTION_NAME = "rooms";
    
    public String roomId;
    public String cinemaId;
    public String name;
    public String layoutType;
    public int seatRows;
    public int seatCols;
    public int totalSeats;
    public String status;
    public long createdAt;
    public long updatedAt;
    public boolean deleted;
}
