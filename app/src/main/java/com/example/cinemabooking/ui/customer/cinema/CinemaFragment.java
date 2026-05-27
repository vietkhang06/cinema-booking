package com.example.cinemabooking.ui.customer.cinema;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.repository.CinemaRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema;
import com.example.cinemabooking.domain.repository.CinemaRepository;
import com.example.cinemabooking.ui.customer.cinema.adapter.CinemaAdapter;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CinemaFragment extends Fragment {

    private static final String ALL_LOCATIONS = "Toan quoc";

    private RecyclerView rvCinemas;
    private TextView tvLocation;
    private TextView tvSubtitle;
    private TextView tvEmpty;
    private CinemaAdapter adapter;
    private CinemaRepository cinemaRepository;

    private final List<Cinema> allCinemas = new ArrayList<>();
    private String selectedLocation = ALL_LOCATIONS;

    public CinemaFragment() {
        super(R.layout.fragment_cinema);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCinemas = view.findViewById(R.id.rvCinemas);
        tvLocation = view.findViewById(R.id.tvCurrentLocation);
        tvSubtitle = view.findViewById(R.id.tvSubtitle);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        cinemaRepository = new CinemaRepositoryImpl();
        adapter = new CinemaAdapter(cinema -> {
            Intent intent = CinemaDetailActivity.createIntent(requireContext(), cinema);
            startActivity(intent);
        });

        rvCinemas.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCinemas.setAdapter(adapter);

        view.findViewById(R.id.layoutSelectLocation).setOnClickListener(v -> showLocationDialog());

        tvLocation.setText("Toan quoc");
        loadCinemas();
    }

    private void loadCinemas() {
        tvEmpty.setVisibility(View.GONE);
        cinemaRepository.getAllCinemas(new ResultCallback<List<Cinema>>() {
            @Override
            public void onSuccess(List<Cinema> result) {
                allCinemas.clear();
                if (result != null) {
                    allCinemas.addAll(result);
                }
                renderCinemas();
            }

            @Override
            public void onError(String message) {
                adapter.submitList(new ArrayList<>());
                tvSubtitle.setText("Khong the tai danh sach rap");
                tvEmpty.setText(message == null || message.trim().isEmpty()
                        ? "Khong the tai danh sach rap phim"
                        : message);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    private void renderCinemas() {
        List<Cinema> filtered = new ArrayList<>();
        for (Cinema cinema : allCinemas) {
            if (cinema != null && matchesLocation(cinema, selectedLocation)) {
                filtered.add(cinema);
            }
        }

        adapter.submitList(filtered);
        tvSubtitle.setText(filtered.size() + " rap dang hoat dong");
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmpty.setText("Chua co rap phim tai khu vuc nay");
    }

    private boolean matchesLocation(Cinema cinema, String location) {
        if (ALL_LOCATIONS.equals(location)) {
            return true;
        }

        String normalizedLocation = normalize(location);
        return normalize(cinema.city).contains(normalizedLocation)
                || normalize(cinema.district).contains(normalizedLocation)
                || normalize(cinema.address).contains(normalizedLocation);
    }

    private void showLocationDialog() {
        LocationBottomSheetFragment sheet =
                LocationBottomSheetFragment.newInstance(selectedLocation);

        sheet.setOnLocationSelectedListener(location -> {
            selectedLocation = location;
            tvLocation.setText(location);
            renderCinemas();
        });

        sheet.show(getParentFragmentManager(), "location_picker");
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.getDefault());
    }
}
