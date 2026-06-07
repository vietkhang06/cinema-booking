package com.example.cinemabooking.domain.model;

public class Voucher {
    public String voucherId;
    public String userId;
    public String voucherType = "SHOWTIME_CANCELLED";
    public Double discountValue;
    public boolean isUsed;
    public long createdAt;

    public Voucher() {
    }
}
