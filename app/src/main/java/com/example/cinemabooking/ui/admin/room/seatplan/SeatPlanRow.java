package com.example.cinemabooking.ui.admin.room.seatplan;

import java.util.List;

public class SeatPlanRow {
    public String rowName;
    public List<com.example.cinemabooking.ui.admin.room.seatplan.SeatPlanCell> cells;

    public SeatPlanRow(String rowName, List<com.example.cinemabooking.ui.admin.room.seatplan.SeatPlanCell> cells) {
        this.rowName = rowName;
        this.cells = cells;
    }
}