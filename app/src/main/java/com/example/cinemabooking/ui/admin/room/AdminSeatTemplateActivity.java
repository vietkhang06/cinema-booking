package com.example.cinemabookingapp.ui.admin.room;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.data.repository.SeatRepositoryImpl;
import com.example.cinemabookingapp.domain.model.SeatTemplate;
import com.example.cinemabookingapp.domain.repository.SeatRepository;
import com.example.cinemabookingapp.ui.admin.room.seatplan.SeatPlanCell;
import com.example.cinemabookingapp.ui.admin.room.seatplan.SeatPlanRow;
import com.example.cinemabookingapp.ui.admin.room.seatplan.SeatPlanRowAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminSeatTemplateActivity extends AppCompatActivity {

    private static final int DEFAULT_ROWS = 6;
    private static final int DEFAULT_COLUMNS = 12;

    private String roomId;
    private SeatRepository seatRepository;

    private final List<SeatPlanRow> seatRows = new ArrayList<>();

    private SeatPlanRowAdapter rowAdapter;
    private int selectedPaintType = SeatPlanCell.TYPE_NORMAL;
    private TextView tvCurrentMode;
    private TextView tvSummary;
    private EditText etRows;
    private EditText etColumns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_seat_template);

        RecyclerView rvSeatRows = findViewById(R.id.rvSeatRows);
        tvCurrentMode = findViewById(R.id.tvSeatCurrentMode);
        tvSummary = findViewById(R.id.tvSeatSummary);
        etRows = findViewById(R.id.etSeatRows);
        etColumns = findViewById(R.id.etSeatColumns);

        android.widget.ImageButton ibBack = findViewById(R.id.ibBack);
        if (ibBack != null) {
            ibBack.setOnClickListener(v -> finish());
        }

        String roomName = getIntent().getStringExtra("extra_room_name");
        TextView tvHeaderSubtitle = findViewById(R.id.tvHeaderSubtitle);
        if (tvHeaderSubtitle != null && roomName != null) {
            tvHeaderSubtitle.setText("Phòng chiếu: " + roomName);
        }

        Button btnModeNormal = findViewById(R.id.btnModeNormal);
        Button btnModeVip = findViewById(R.id.btnModeVip);
        Button btnModeCouple = findViewById(R.id.btnModeCouple);
        Button btnModeLocked = findViewById(R.id.btnModeLocked);
        Button btnGenerate = findViewById(R.id.btnGenerateSeatPlan);
        Button btnReset = findViewById(R.id.btnResetSeatPlan);
        Button btnSave = findViewById(R.id.btnSaveSeatPlan);

        rvSeatRows.setLayoutManager(new LinearLayoutManager(this));
        rvSeatRows.setHasFixedSize(true);

        rowAdapter = new SeatPlanRowAdapter(seatRows, (rowPosition, seatPosition) -> {
            SeatPlanCell cell = seatRows.get(rowPosition).cells.get(seatPosition);
            cell.type = selectedPaintType;
            rowAdapter.notifyItemChanged(rowPosition);
            updateSummary();
        });

        rvSeatRows.setAdapter(rowAdapter);

        btnModeNormal.setOnClickListener(v -> setPaintMode(SeatPlanCell.TYPE_NORMAL));
        btnModeVip.setOnClickListener(v -> setPaintMode(SeatPlanCell.TYPE_VIP));
        btnModeCouple.setOnClickListener(v -> setPaintMode(SeatPlanCell.TYPE_COUPLE));
        btnModeLocked.setOnClickListener(v -> setPaintMode(SeatPlanCell.TYPE_LOCKED));

        btnGenerate.setOnClickListener(v -> generateFromInputs());
        btnReset.setOnClickListener(v -> resetTemplate());
        btnSave.setOnClickListener(v -> saveSeatTemplates());

        setPaintMode(SeatPlanCell.TYPE_NORMAL);

        roomId = getIntent().getStringExtra("extra_room_id");
        seatRepository = new SeatRepositoryImpl();

        int rows = getIntent().getIntExtra("extra_rows", DEFAULT_ROWS);
        int cols = getIntent().getIntExtra("extra_cols", DEFAULT_COLUMNS);
        if (etRows != null) etRows.setText(String.valueOf(rows));
        if (etColumns != null) etColumns.setText(String.valueOf(cols));

        if (roomId != null) {
            loadSeatTemplates();
        } else {
            generateTemplate(rows, cols);
        }
    }

    private void setPaintMode(int type) {
        selectedPaintType = type;
        tvCurrentMode.setText("Chế độ hiện tại: " + modeName(type));
    }

    private void generateFromInputs() {
        int rows = parsePositiveInt(etRows.getText().toString(), DEFAULT_ROWS);
        int cols = parsePositiveInt(etColumns.getText().toString(), DEFAULT_COLUMNS);

        if (rows < 1) rows = DEFAULT_ROWS;
        if (cols < 1) cols = DEFAULT_COLUMNS;

        if (rows > 26) rows = 26;
        if (cols > 20) cols = 20;

        generateTemplate(rows, cols);
        Toast.makeText(this, "Đã tạo sơ đồ ghế " + rows + " x " + cols, Toast.LENGTH_SHORT).show();
    }

    private void resetTemplate() {
        etRows.setText(String.valueOf(DEFAULT_ROWS));
        etColumns.setText(String.valueOf(DEFAULT_COLUMNS));
        setPaintMode(SeatPlanCell.TYPE_NORMAL);
        generateTemplate(DEFAULT_ROWS, DEFAULT_COLUMNS);
        Toast.makeText(this, "Đã reset sơ đồ ghế", Toast.LENGTH_SHORT).show();
    }

    private void generateTemplate(int rowCount, int columnCount) {
        seatRows.clear();

        for (int r = 0; r < rowCount; r++) {
            String rowName = String.valueOf((char) ('A' + r));
            List<SeatPlanCell> cells = new ArrayList<>();

            for (int c = 1; c <= columnCount; c++) {
                String seatCode = rowName + String.format(Locale.getDefault(), "%02d", c);

                int type = SeatPlanCell.TYPE_NORMAL;

                // Tạo điểm nhấn mặc định: Hàng C và D là VIP
                if (rowName.equals("C") || rowName.equals("D")) {
                    type = SeatPlanCell.TYPE_VIP;
                }

                cells.add(new SeatPlanCell(seatCode, type));
            }

            seatRows.add(new SeatPlanRow(rowName, cells));
        }

        rowAdapter.notifyDataSetChanged();
        updateSummary();
    }

    private void loadSeatTemplates() {
        if (roomId == null) return;

        seatRepository.getSeatTemplatesByRoomId(roomId, new com.example.cinemabookingapp.domain.common.ResultCallback<List<SeatTemplate>>() {
            @Override
            public void onSuccess(List<SeatTemplate> templates) {
                if (isFinishing() || isDestroyed()) return;

                if (templates != null && !templates.isEmpty()) {
                    populateFromTemplates(templates);
                } else {
                    int rows = getIntent().getIntExtra("extra_rows", DEFAULT_ROWS);
                    int cols = getIntent().getIntExtra("extra_cols", DEFAULT_COLUMNS);
                    generateTemplate(rows, cols);
                }
            }

            @Override
            public void onError(String message) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(AdminSeatTemplateActivity.this, "Lỗi tải sơ đồ ghế: " + message, Toast.LENGTH_SHORT).show();

                int rows = getIntent().getIntExtra("extra_rows", DEFAULT_ROWS);
                int cols = getIntent().getIntExtra("extra_cols", DEFAULT_COLUMNS);
                generateTemplate(rows, cols);
            }
        });
    }

    private void populateFromTemplates(List<SeatTemplate> templates) {
        seatRows.clear();

        int maxRow = 0;
        int maxCol = 0;

        java.util.Map<String, List<SeatTemplate>> groups = new java.util.TreeMap<>();
        for (SeatTemplate t : templates) {
            if (t.rowName == null) continue;
            if (!groups.containsKey(t.rowName)) {
                groups.put(t.rowName, new ArrayList<>());
            }
            groups.get(t.rowName).add(t);

            int rIdx = t.rowName.charAt(0) - 'A';
            if (rIdx > maxRow) maxRow = rIdx;
            if (t.columnNo > maxCol) maxCol = t.columnNo;
        }

        maxRow += 1;

        if (etRows != null) etRows.setText(String.valueOf(maxRow));
        if (etColumns != null) etColumns.setText(String.valueOf(maxCol));

        for (java.util.Map.Entry<String, List<SeatTemplate>> entry : groups.entrySet()) {
            String rowName = entry.getKey();
            List<SeatTemplate> rowTemplates = entry.getValue();

            rowTemplates.sort((a, b) -> Integer.compare(a.columnNo, b.columnNo));

            List<SeatPlanCell> cells = new ArrayList<>();
            for (SeatTemplate t : rowTemplates) {
                int cellType = SeatPlanCell.TYPE_NORMAL;
                if (!t.isEnabled) {
                    cellType = SeatPlanCell.TYPE_LOCKED;
                } else if ("VIP".equalsIgnoreCase(t.seatType)) {
                    cellType = SeatPlanCell.TYPE_VIP;
                } else if ("COUPLE".equalsIgnoreCase(t.seatType)) {
                    cellType = SeatPlanCell.TYPE_COUPLE;
                }

                cells.add(new SeatPlanCell(t.seatCode, cellType));
            }

            seatRows.add(new SeatPlanRow(rowName, cells));
        }

        rowAdapter.notifyDataSetChanged();
        updateSummary();
    }

    private void saveSeatTemplates() {
        if (roomId == null) {
            Toast.makeText(this, "Không tìm thấy Room ID để lưu", Toast.LENGTH_SHORT).show();
            return;
        }

        List<SeatTemplate> templates = new ArrayList<>();
        for (SeatPlanRow row : seatRows) {
            for (int i = 0; i < row.cells.size(); i++) {
                SeatPlanCell cell = row.cells.get(i);
                SeatTemplate t = new SeatTemplate();
                t.roomId = roomId;
                t.seatCode = cell.seatCode;
                t.rowName = row.rowName;
                t.columnNo = i + 1;

                if (cell.type == SeatPlanCell.TYPE_LOCKED) {
                    t.seatType = "STANDARD";
                    t.isEnabled = false;
                } else {
                    t.isEnabled = true;
                    if (cell.type == SeatPlanCell.TYPE_VIP) {
                        t.seatType = "VIP";
                    } else if (cell.type == SeatPlanCell.TYPE_COUPLE) {
                        t.seatType = "COUPLE";
                    } else {
                        t.seatType = "STANDARD";
                    }
                }

                t.seatId = roomId + "_" + t.seatCode;
                templates.add(t);
            }
        }

        seatRepository.createSeatTemplates(roomId, templates, new com.example.cinemabookingapp.domain.common.ResultCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(AdminSeatTemplateActivity.this, "Đã lưu sơ đồ ghế thành công", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(AdminSeatTemplateActivity.this, "Lỗi khi lưu sơ đồ ghế: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSummary() {
        int normal = 0;
        int vip = 0;
        int couple = 0;
        int locked = 0;

        for (SeatPlanRow row : seatRows) {
            for (SeatPlanCell cell : row.cells) {
                switch (cell.type) {
                    case SeatPlanCell.TYPE_VIP:
                        vip++;
                        break;
                    case SeatPlanCell.TYPE_COUPLE:
                        couple++;
                        break;
                    case SeatPlanCell.TYPE_LOCKED:
                        locked++;
                        break;
                    default:
                        normal++;
                        break;
                }
            }
        }

        tvSummary.setText("Normal: " + normal +
                " | VIP: " + vip +
                " | Couple: " + couple +
                " | Locked: " + locked);
    }

    private int parsePositiveInt(String value, int fallback) {
        try {
            int result = Integer.parseInt(value.trim());
            return result > 0 ? result : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }

    private String modeName(int type) {
        switch (type) {
            case SeatPlanCell.TYPE_VIP:
                return "VIP";
            case SeatPlanCell.TYPE_COUPLE:
                return "COUPLE";
            case SeatPlanCell.TYPE_LOCKED:
                return "LOCKED";
            default:
                return "NORMAL";
        }
    }
}