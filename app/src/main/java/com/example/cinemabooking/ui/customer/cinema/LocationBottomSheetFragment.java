
package com.example.cinemabooking.ui.customer.cinema;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.repository.CinemaRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.repository.CinemaRepository;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class LocationBottomSheetFragment extends BottomSheetDialogFragment {

    public interface OnLocationSelectedListener {
        void onLocationSelected(String location);
    }

    private OnLocationSelectedListener listener;
    private String currentSelected;

    private LocationFilterAdapter adapter;
    private final List<String> allLocations = new ArrayList<>();

    public static LocationBottomSheetFragment newInstance(String currentSelected) {
        LocationBottomSheetFragment f = new LocationBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("selected", currentSelected);
        f.setArguments(args);
        return f;
    }

    public void setOnLocationSelectedListener(OnLocationSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentSelected = getArguments().getString("selected", "Toan quoc");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etSearch = view.findViewById(R.id.etSearchLocation);
        RecyclerView rvLocations = view.findViewById(R.id.rvLocations);
        TextView tvStatus = view.findViewById(R.id.tvLocationStatus);

        adapter = new LocationFilterAdapter(currentSelected, location -> {
            if (listener != null) listener.onLocationSelected(location);
            dismiss();
        });

        rvLocations.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvLocations.setAdapter(adapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Load cities từ Firebase (lấy từ danh sách cinema)
        tvStatus.setText("Đang tải...");
        CinemaRepository repo = new CinemaRepositoryImpl();
        repo.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> result) {
                LinkedHashSet<String> citySet = new LinkedHashSet<>();
                citySet.add("Toan quoc");
                if (result != null) {
                    for (Cinema c : result) {
                        if (c.city != null && !c.city.trim().isEmpty()) {
                            citySet.add(c.city.trim());
                        }
                    }
                }
                allLocations.clear();
                allLocations.addAll(citySet);
                adapter.setData(allLocations);
                tvStatus.setText(citySet.size() - 1 + " tỉnh thành");
            }

            @Override
            public void onError(String message) {
                tvStatus.setText("Lỗi tải dữ liệu");
                // fallback cứng
                allLocations.clear();
                allLocations.add("Toan quoc");
                allLocations.add("TP Ho Chi Minh");
                allLocations.add("Ha Noi");
                allLocations.add("Da Nang");
                adapter.setData(allLocations);
            }
        });
    }

    private void filterList(String query) {
        if (query.trim().isEmpty()) {
            adapter.setData(allLocations);
            return;
        }
        String normalized = normalize(query);
        List<String> filtered = new ArrayList<>();
        for (String loc : allLocations) {
            if (normalize(loc).contains(normalized)) {
                filtered.add(loc);
            }
        }
        adapter.setData(filtered);
    }

    private String normalize(String value) {
        if (value == null) return "";
        String n = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        return n.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.getDefault());
    }
}