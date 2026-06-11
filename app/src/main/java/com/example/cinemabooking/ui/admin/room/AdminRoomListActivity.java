package com.example.cinemabooking.ui.admin.room;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.data.repository.CinemaRepositoryImpl;
import com.example.cinemabooking.data.repository.RoomRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.model.Room;
import com.example.cinemabooking.domain.repository.CinemaRepository;
import com.example.cinemabooking.domain.repository.RoomRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class AdminRoomListActivity extends BaseActivity {

    private MaterialAutoCompleteTextView actvCinemaChooser;
    private MaterialButton btnAddRoom;
    private RecyclerView rvRooms;
    private TextView tvEmptyRooms;

    private CinemaRepository cinemaRepository;
    private RoomRepository roomRepository;

    private AdminRoomAdapter roomAdapter;
    private List<Cinema> cinemaList = new ArrayList<>();
    private Cinema selectedCinema;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_room_list);

        cinemaRepository = new CinemaRepositoryImpl();
        roomRepository = new RoomRepositoryImpl();

        initViews();
        setupActions();
        loadCinemas();

        if (getIntent().getBooleanExtra("extra_from_seat_menu", false)) {
            showToast("Vui lòng chọn rạp chiếu để bắt đầu thiết lập sơ đồ ghế.");
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        loadRooms();
    }
    private void initViews() {
        actvCinemaChooser = findViewById(R.id.actvCinemaChooser);
        btnAddRoom = findViewById(R.id.btnAddRoom);
        rvRooms = findViewById(R.id.rvRooms);
        tvEmptyRooms = findViewById(R.id.tvEmptyRooms);

        roomAdapter = new AdminRoomAdapter();
        rvRooms.setLayoutManager(new LinearLayoutManager(this));
        rvRooms.setAdapter(roomAdapter);
    }

    private void setupActions() {
        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnAddRoom.setOnClickListener(v -> {
            if (selectedCinema != null) {
                showRoomFormDialog(null);
            }
        });

        roomAdapter.setListener(new AdminRoomAdapter.OnRoomActionListener() {
            @Override
            public void onEditClick(Room room) {
                showRoomFormDialog(room);
            }

            @Override
            public void onDeleteClick(Room room) {
                showDeleteRoomConfirmDialog(room);
            }

            @Override
            public void onViewSeatsClick(Room room) {
                Intent intent = new Intent(AdminRoomListActivity.this, AdminSeatTemplateActivity.class);
                intent.putExtra("extra_room_id", room.roomId);
                intent.putExtra("extra_room_name", room.name);
                intent.putExtra("extra_rows", room.seatRows);
                intent.putExtra("extra_cols", room.seatCols);
                startActivity(intent);
            }
        });

        actvCinemaChooser.setOnItemClickListener((parent, view, position, id) -> {
            selectedCinema = cinemaList.get(position);
            btnAddRoom.setVisibility(View.VISIBLE);
            loadRooms();
        });
    }

    private void loadCinemas() {
        cinemaRepository.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> data) {
                if (isFinishing() || isDestroyed()) return;
                cinemaList.clear();
                if (data != null) {
                    for (Cinema c : data) {
                        if (!c.deleted) {
                            cinemaList.add(c);
                        }
                    }
                }

                List<String> names = new ArrayList<>();
                for (Cinema c : cinemaList) {
                    names.add(c.name);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AdminRoomListActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        names
                );
                actvCinemaChooser.setAdapter(adapter);
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải rạp chiếu: " + message);
            }
        });
    }

    private void loadRooms() {
        if (selectedCinema == null) return;

        roomRepository.getRoomsByCinemaId(selectedCinema.cinemaId, new ResultCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> data) {
                if (isFinishing() || isDestroyed()) return;
                roomAdapter.submitList(data);

                if (data == null || data.isEmpty()) {
                    tvEmptyRooms.setText("Rạp này chưa có phòng chiếu nào");
                    tvEmptyRooms.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyRooms.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String message) {
                showToast("Lỗi tải phòng chiếu: " + message);
            }
        });
    }

    private void showRoomFormDialog(final Room roomToEdit) {
        if (selectedCinema == null) return;
        boolean isEdit = roomToEdit != null;
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_room_form, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        com.google.android.material.textfield.TextInputEditText edtName = dialogView.findViewById(R.id.edtRoomName);
        MaterialAutoCompleteTextView actvLayout = dialogView.findViewById(R.id.actvRoomLayout);
        com.google.android.material.textfield.TextInputEditText edtRows = dialogView.findViewById(R.id.edtRoomRows);
        com.google.android.material.textfield.TextInputEditText edtCols = dialogView.findViewById(R.id.edtRoomCols);
        MaterialAutoCompleteTextView actvStatus = dialogView.findViewById(R.id.actvRoomStatus);

        View btnCancel = dialogView.findViewById(R.id.btnCancel);
        View btnSave = dialogView.findViewById(R.id.btnSave);

        // Setup dropdowns
        String[] layouts = {"2D", "3D", "IMAX"};
        actvLayout.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, layouts));

        String[] statuses = {"active", "inactive"};
        actvStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses));

        if (isEdit) {
            tvTitle.setText("Chỉnh sửa phòng chiếu");
            edtName.setText(roomToEdit.name);
            actvLayout.setText(roomToEdit.layoutType, false);
            edtRows.setText(String.valueOf(roomToEdit.seatRows));
            edtCols.setText(String.valueOf(roomToEdit.seatCols));
            actvStatus.setText(roomToEdit.status, false);
        } else {
            tvTitle.setText("Thêm phòng chiếu mới");
            actvLayout.setText("2D", false);
            edtRows.setText("6");
            edtCols.setText("6");
            actvStatus.setText("active", false);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnSave.setOnClickListener(v -> {
            String name = edtName.getText() == null ? "" : edtName.getText().toString().trim();
            String layoutType = actvLayout.getText().toString().trim();
            String rowsStr = edtRows.getText() == null ? "" : edtRows.getText().toString().trim();
            String colsStr = edtCols.getText() == null ? "" : edtCols.getText().toString().trim();
            String status = actvStatus.getText().toString().trim();

            if (TextUtils.isEmpty(name)) {
                edtName.setError("Nhập tên phòng chiếu");
                return;
            }

            int rows = 6;
            int cols = 6;
            try {
                rows = Integer.parseInt(rowsStr);
                if (rows < 6 || rows > 10) {
                    edtRows.setError("Số hàng phải từ 6 đến 10");
                    return;
                }
            } catch (NumberFormatException e) {
                edtRows.setError("Số hàng không hợp lệ");
                return;
            }

            try {
                cols = Integer.parseInt(colsStr);
                if (cols < 6 || cols > 10) {
                    edtCols.setError("Số cột phải từ 6 đến 10");
                    return;
                }
            } catch (NumberFormatException e) {
                edtCols.setError("Số cột không hợp lệ");
                return;
            }


            Room r = isEdit ? roomToEdit : new Room();
            r.cinemaId = selectedCinema.cinemaId;
            r.name = name;
            r.layoutType = layoutType;
            r.seatRows = rows;
            r.seatCols = cols;
            r.totalSeats = rows * cols;
            r.status = status;

            if (isEdit) {
                roomRepository.updateRoom(r, new ResultCallback<Room>() {
                    @Override
                    public void onSuccess(Room data) {
                        showToast("Đã cập nhật phòng chiếu");
                        dialog.dismiss();
                        loadRooms();
                    }

                    @Override
                    public void onError(String message) {
                        showToast("Lỗi: " + message);
                    }
                });
            } else {
                roomRepository.createRoom(r, new ResultCallback<Room>() {
                    @Override
                    public void onSuccess(Room data) {
                        showToast("Đã thêm phòng chiếu");
                        dialog.dismiss();
                        loadRooms();
                    }

                    @Override
                    public void onError(String message) {
                        showToast("Lỗi: " + message);
                    }
                });
            }
        });

        dialog.show();
    }

    private void showDeleteRoomConfirmDialog(final Room room) {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Xác nhận xóa phòng")
                .setMessage("Bạn có chắc chắn muốn xóa phòng '" + room.name + "' không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    roomRepository.softDeleteRoom(room.roomId, new ResultCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            showToast("Đã xóa phòng chiếu thành công");
                            loadRooms();
                        }

                        @Override
                        public void onError(String message) {
                            showToast("Lỗi: " + message);
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}