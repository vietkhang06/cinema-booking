package com.example.cinemabooking.ui.admin.cinema;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.data.repository.CinemaRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.ui.admin.AdminBottomNavHelper;
import com.example.cinemabooking.ui.admin.cinema.adapter.AdminCinemaAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AdminCinemaListActivity extends BaseActivity {

    private RecyclerView rv;
    private TextView tvEmpty;
    private MaterialButton btnAdd;

    private final List<Cinema> list = new ArrayList<>();
    private AdminCinemaAdapter adapter;

    private CinemaRepositoryImpl repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_cinema_list);

        AdminBottomNavHelper.setupAdminBottomNavigation(this, 1);

        repo = new CinemaRepositoryImpl();

        rv = findViewById(R.id.rvCinema);
        tvEmpty = findViewById(R.id.tvEmpty);
        btnAdd = findViewById(R.id.btnAddCinema);

        View btnBack = findViewById(R.id.btnAdminBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        adapter = new AdminCinemaAdapter();
        adapter.setListener(new AdminCinemaAdapter.OnCinemaActionListener() {
            @Override
            public void onEditClick(Cinema cinema) {
                Intent intent = new Intent(AdminCinemaListActivity.this, AdminCinemaFormActivity.class);
                intent.putExtra(AdminCinemaFormActivity.EXTRA_CINEMA_ID, cinema.cinemaId);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Cinema cinema) {
                new androidx.appcompat.app.AlertDialog.Builder(AdminCinemaListActivity.this)
                        .setTitle("⚠️ Xác nhận xóa rạp")
                        .setMessage("Bạn có chắc chắn muốn xóa rạp '" + cinema.name + "' không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            repo.softDeleteCinema(cinema.cinemaId, new ResultCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    showToast("Đã xóa rạp thành công");
                                    loadData();
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

            @Override
            public void onViewDetailsClick(Cinema cinema) {
                Intent intent = new Intent(AdminCinemaListActivity.this, AdminCinemaDetailActivity.class);
                intent.putExtra(AdminCinemaDetailActivity.EXTRA_CINEMA_ID, cinema.cinemaId);
                startActivity(intent);
            }
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AdminCinemaFormActivity.class))
        );
        
        android.widget.EditText etSearch = findViewById(R.id.etSearchCinema);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString());
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
    
    private void filter(String text) {
        if (text.isEmpty()) {
            adapter.submitList(new ArrayList<>(list));
            return;
        }
        String lowerText = text.toLowerCase();
        List<Cinema> filtered = new ArrayList<>();
        for (Cinema c : list) {
            if ((c.name != null && c.name.toLowerCase().contains(lowerText)) || 
                (c.city != null && c.city.toLowerCase().contains(lowerText))) {
                filtered.add(c);
            }
        }
        adapter.submitList(filtered);
    }

    private void loadData() {
        repo.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> data) {

                if (isFinishing() || isDestroyed()) return;

                list.clear();
                if (data != null) list.addAll(data);

                adapter.submitList(new ArrayList<>(list));

                tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String message) {
                showToast(message);
            }
        });
    }
}