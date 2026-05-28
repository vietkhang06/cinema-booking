package com.example.cinemabooking.data.dto;

import java.util.List;

public class SeatBookingRequestDTO {
    public String showtimeId;
    public List<String> seatIds;
    public List<SnackOrder> snackOrders;
    public String paymentMethod;

    public SeatBookingRequestDTO() {
    }

    public SeatBookingRequestDTO(String showtimeId, List<String> seatIds, List<SnackOrder> snackOrders, String paymentMethod) {
        this.showtimeId = showtimeId;
        this.seatIds = seatIds;
        this.snackOrders = snackOrders;
        this.paymentMethod = paymentMethod;
    }

    public static class SnackOrder {
        public String snackId;
        public int quantity;

        public SnackOrder() {
        }

        public SnackOrder(String snackId, int quantity) {
            this.snackId = snackId;
            this.quantity = quantity;
        }
    }
}
