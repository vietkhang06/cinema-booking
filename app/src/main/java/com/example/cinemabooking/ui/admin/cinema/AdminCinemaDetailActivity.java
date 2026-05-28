package com.example.cinemabooking.ui.admin.cinema;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.data.repository.CinemaRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.repository.CinemaRepository;
import com.google.android.material.button.MaterialButton;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.data.repository.RoomRepositoryImpl;
import com.example.cinemabooking.domain.model.Room;
import com.example.cinemabooking.domain.repository.RoomRepository;
import com.example.cinemabooking.ui.admin.room.AdminRoomAdapter;
import com.example.cinemabooking.ui.admin.room.AdminSeatTemplateActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminCinemaDetailActivity extends BaseActivity {

    public static final String EXTRA_CINEMA_ID = "extra_cinema_id";

    private TextView tvName, tvAddress, tvCity, tvDistrict, tvPhone, tvLat, tvLng, tvStatus;
    private android.view.View btnEdit;
    private com.google.android.material.button.MaterialButton btnDelete;

    private RecyclerView rvRooms;
    private TextView tvEmptyRooms;
    private com.google.android.material.button.MaterialButton btnAddRoom;
    private AdminRoomAdapter roomAdapter;

    private CinemaRepository cinemaRepository;
    private RoomRepository roomRepository;
    private String cinemaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_cinema_detail);

        cinemaRepository = new CinemaRepositoryImpl();
        roomRepository = new RoomRepositoryImpl();

        cinemaId = getIntent().getStringExtra(EXTRA_CINEMA_ID);

        if (cinemaId == null) {
            showToast("Thiếu cinemaId");
            finish();
            return;
        }

        initViews();
        setupActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
        loadRooms();
    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvAddress = findViewById(R.id.tvAddress);
        tvCity = findViewById(R.id.tvCity);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvPhone = findViewById(R.id.tvPhone);
        tvLat = findViewById(R.id.tvLat);
        tvLng = findViewById(R.id.tvLng);
        tvStatus = findViewById(R.id.tvStatus);

        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        rvRooms = findViewById(R.id.rvRooms);
        tvEmptyRooms = findViewById(R.id.tvEmptyRooms);
        btnAddRoom = findViewById(R.id.btnAddRoom);

        roomAdapter = new AdminRoomAdapter();
        if (rvRooms != null) {
            rvRooms.setLayoutManager(new LinearLayoutManager(this));
            rvRooms.setAdapter(roomAdapter);
        }
    }

    private void setupActions() {
        android.view.View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminCinemaFormActivity.class);
                intent.putExtra(AdminCinemaFormActivity.EXTRA_CINEMA_ID, cinemaId);
                startActivity(intent);
            });
        }

        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("⚠️ Xác nhận xóa rạp")
                        .setMessage("Bạn có chắc chắn muốn xóa rạp này không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            cinemaRepository.softDeleteCinema(cinemaId, new ResultCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    showToast("Đã xóa rạp thành công");
                                    finish();
                                }

                                @Override
                                public void onError(String message) {
                                    showToast("Lỗi: " + message);
                                }
                            });
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        if (btnAddRoom != null) {
            btnAddRoom.setOnClickListener(v -> showRoomFormDialog(null));
        }

        if (roomAdapter != null) {
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
                    Intent intent = new Intent(AdminCinemaDetailActivity.this, AdminSeatTemplateActivity.class);
                    intent.putExtra("extra_room_id", room.roomId);
                    intent.putExtra("extra_room_name", room.name);
                    intent.putExtra("extra_rows", room.seatRows);
                    intent.putExtra("extra_cols", room.seatCols);
                    startActivity(intent);
                }
            });
        }
    }

    private void loadRooms() {
        if (roomRepository == null || cinemaId == null) return;
        roomRepository.getRoomsByCinemaId(cinemaId, new ResultCallback<List<Room>>() {
            @Override
            public void onSuccess(List<Room> data) {
                if (isFinishing() || isDestroyed()) return;
                roomAdapter.submitList(data);
                if (tvEmptyRooms != null) {
                    tvEmptyRooms.setVisibility(data == null || data.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                }
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }

    private void showRoomFormDialog(final Room roomToEdit) {
        boolean isEdit = roomToEdit != null;
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_admin_room_form, null);

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        com.google.android.material.textfield.TextInputEditText edtName = dialogView.findViewById(R.id.edtRoomName);
        com.google.android.material.textfield.MaterialAutoCompleteTextView actvLayout = dialogView.findViewById(R.id.actvRoomLayout);
        com.google.android.material.textfield.TextInputEditText edtRows = dialogView.findViewById(R.id.edtRoomRows);
        com.google.android.material.textfield.TextInputEditText edtCols = dialogView.findViewById(R.id.edtRoomCols);
        com.google.android.material.textfield.MaterialAutoCompleteTextView actvStatus = dialogView.findViewById(R.id.actvRoomStatus);

        android.view.View btnCancel = dialogView.findViewById(R.id.btnCancel);
        android.view.View btnSave = dialogView.findViewById(R.id.btnSave);

        // Setup dropdowns
        String[] layouts = {"2D", "3D", "IMAX"};
        actvLayout.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, layouts));

        String[] statuses = {"active", "inactive"};
        actvStatus.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, statuses));

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
            edtCols.setText("12");
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
            int cols = 12;
            try {
                rows = Integer.parseInt(rowsStr);
                if (rows <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                edtRows.setError("Số hàng không hợp lệ");
                return;
            }

            try {
                cols = Integer.parseInt(colsStr);
                if (cols <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                edtCols.setError("Số cột không hợp lệ");
                return;
            }

            Room r = isEdit ? roomToEdit : new Room();
            r.cinemaId = cinemaId;
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

    private void loadData() {
        cinemaRepository.getCinemaById(cinemaId, new ResultCallback<Cinema>() {
            @Override
            public void onSuccess(Cinema c) {
                if (c == null) return;

                tvName.setText(c.name);
                tvAddress.setText(c.address);
                tvCity.setText(c.city);
                tvDistrict.setText(c.district);
                tvPhone.setText(c.phone);
                tvLat.setText(String.valueOf(c.latitude));
                tvLng.setText(String.valueOf(c.longitude));
                tvStatus.setText(c.status);
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }
}