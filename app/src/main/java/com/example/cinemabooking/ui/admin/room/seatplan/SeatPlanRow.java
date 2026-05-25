package com.example.cinemabooking.ui.admin.room.seatplan;

import java.util.List;

public class SeatPlanRow {
    public String rowName;
    public List<SeatPlanCell> cells;

    public SeatPlanRow(String rowName, List<SeatPlanCell> cells) {
        this.rowName = rowName;
        this.cells = cells;
    }
}