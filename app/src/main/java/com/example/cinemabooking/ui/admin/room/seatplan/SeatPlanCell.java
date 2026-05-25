package com.example.cinemabooking.ui.admin.room.seatplan;

public class SeatPlanCell {
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_VIP = 1;
    public static final int TYPE_COUPLE = 2;
    public static final int TYPE_LOCKED = 3;

    public String seatCode;
    public int type;

    public SeatPlanCell(String seatCode, int type) {
        this.seatCode = seatCode;
        this.type = type;
    }
}