package com.example.cinemabooking.ui.admin.notification;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cinemabooking.R;
import com.example.cinemabooking.data.repository.UserRepositoryImpl;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.User;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AdminSelectUserActivity extends AppCompatActivity {

    private static final String TAG = "AdminSelectUserActivity";

    private EditText etSearchUser;
    private ChipGroup chipGroupFilters;
    private RecyclerView rvCustomers;
    private View layoutEmptyState;
    private TextView tvSelectCount;
    private Button btnSelectAll, btnConfirmSelection;

    private UserRepositoryImpl userRepository;
    private final List<User> fullCustomerList = new ArrayList<>();
    private final List<User> filteredCustomerList = new ArrayList<>();
    private final Set<String> selectedUids = new HashSet<>();
    private CustomerSelectAdapter adapter;

    private String currentSearchQuery = "";
    private int currentFilterId = R.id.chipAll;
    private boolean isAllSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_select_user);

        userRepository = new UserRepositoryImpl();

        // Get previously selected uids from Intent
        ArrayList<String> previousUids = getIntent().getStringArrayListExtra("SELECTED_UIDS");
        if (previousUids != null) {
            selectedUids.addAll(previousUids);
        }

        initViews();
        setupListeners();
        loadCustomers();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        etSearchUser = findViewById(R.id.etSearchUser);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        rvCustomers = findViewById(R.id.rvCustomers);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvSelectCount = findViewById(R.id.tvSelectCount);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnConfirmSelection = findViewById(R.id.btnConfirmSelection);

        rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerSelectAdapter(filteredCustomerList);
        rvCustomers.setAdapter(adapter);

        updateSelectCount();
    }

    private void setupListeners() {
        etSearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim().toLowerCase();
                applyFiltersAndSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                chipGroupFilters.check(R.id.chipAll);
                currentFilterId = R.id.chipAll;
            } else {
                currentFilterId = checkedId;
            }
            applyFiltersAndSearch();
        });

        btnSelectAll.setOnClickListener(v -> {
            if (!isAllSelected) {
                // Select all in current filter
                for (User u : filteredCustomerList) {
                    selectedUids.add(u.uid);
                }
                isAllSelected = true;
                btnSelectAll.setText("Hủy chọn tất cả");
            } else {
                // Deselect all in current filter
                for (User u : filteredCustomerList) {
                    selectedUids.remove(u.uid);
                }
                isAllSelected = false;
                btnSelectAll.setText("Chọn tất cả");
            }
            updateSelectCount();
            adapter.notifyDataSetChanged();
        });

        btnConfirmSelection.setOnClickListener(v -> {
            ArrayList<String> uidList = new ArrayList<>(selectedUids);
            
            // Also pass back names or emails for display in main activity
            ArrayList<String> displayNames = new ArrayList<>();
            for (String uid : uidList) {
                for (User u : fullCustomerList) {
                    if (u.uid.equals(uid)) {
                        displayNames.add(getUserDisplayName(u));
                        break;
                    }
                }
            }

            Intent resultIntent = new Intent();
            resultIntent.putStringArrayListExtra("SELECTED_UIDS", uidList);
            resultIntent.putStringArrayListExtra("SELECTED_NAMES", displayNames);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void loadCustomers() {
        userRepository.getAllUsers(new ResultCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                fullCustomerList.clear();
                if (users != null) {
                    for (User u : users) {
                        if (!u.deleted && "customer".equalsIgnoreCase(u.role)) {
                            fullCustomerList.add(u);
                        }
                    }
                }
                applyFiltersAndSearch();
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Lỗi tải khách hàng: " + message);
                Toast.makeText(AdminSelectUserActivity.this, "Lỗi tải dữ liệu khách hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFiltersAndSearch() {
        filteredCustomerList.clear();

        for (User u : fullCustomerList) {
            boolean matchesSearch = true;
            if (!currentSearchQuery.isEmpty()) {
                String name = u.name != null ? u.name.toLowerCase() : "";
                String phone = u.phone != null ? u.phone.toLowerCase() : "";
                String email = u.email != null ? u.email.toLowerCase() : "";
                matchesSearch = name.contains(currentSearchQuery) || phone.contains(currentSearchQuery) || email.contains(currentSearchQuery);
            }

            boolean matchesFilter = true;
            String level = u.memberLevel != null ? u.memberLevel.toLowerCase() : "standard";
            if ("basic".equals(level)) {
                level = "standard";
            }

            if (currentFilterId == R.id.chipStandard) {
                matchesFilter = "standard".equals(level);
            } else if (currentFilterId == R.id.chipVip) {
                matchesFilter = "vip".equals(level);
            } else if (currentFilterId == R.id.chipGold) {
                matchesFilter = "gold".equals(level);
            }

            if (matchesSearch && matchesFilter) {
                filteredCustomerList.add(u);
            }
        }

        // Check if all current filtered items are in selectedUids
        checkIfAllSelected();
        
        adapter.notifyDataSetChanged();

        if (filteredCustomerList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            rvCustomers.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            rvCustomers.setVisibility(View.VISIBLE);
        }
    }

    private void checkIfAllSelected() {
        if (filteredCustomerList.isEmpty()) {
            isAllSelected = false;
        } else {
            boolean allIn = true;
            for (User u : filteredCustomerList) {
                if (!selectedUids.contains(u.uid)) {
                    allIn = false;
                    break;
                }
            }
            isAllSelected = allIn;
        }

        if (isAllSelected) {
            btnSelectAll.setText("Hủy chọn tất cả");
        } else {
            btnSelectAll.setText("Chọn tất cả");
        }
    }

    private void updateSelectCount() {
        tvSelectCount.setText("Đã chọn: " + selectedUids.size());
    }

    private String getUserDisplayName(User user) {
        if (user.name != null && !user.name.isEmpty()) {
            return user.name;
        }
        if (user.email != null && !user.email.isEmpty()) {
            return user.email;
        }
        if (user.phone != null && !user.phone.isEmpty()) {
            return user.phone;
        }
        return "ID: " + user.uid;
    }

    // RecyclerView Adapter
    public class CustomerSelectAdapter extends RecyclerView.Adapter<CustomerSelectAdapter.ViewHolder> {
        private final List<User> items;

        public CustomerSelectAdapter(List<User> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user_selectable, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User u = items.get(position);

            holder.tvName.setText(u.name != null ? u.name : "Khách hàng");
            
            String contact = "";
            if (u.phone != null && !u.phone.isEmpty()) {
                contact += u.phone;
            }
            if (u.email != null && !u.email.isEmpty()) {
                if (!contact.isEmpty()) contact += " • ";
                contact += u.email;
            }
            holder.tvContact.setText(contact.isEmpty() ? "Không có thông tin liên hệ" : contact);

            // Level styling
            String level = u.memberLevel != null ? u.memberLevel.toUpperCase(Locale.getDefault()) : "STANDARD";
            if ("BASIC".equals(level)) {
                level = "STANDARD";
            }
            holder.tvLevel.setText(level);
            if ("GOLD".equals(level)) {
                holder.tvLevel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF3CD")));
                holder.tvLevel.setTextColor(Color.parseColor("#856404"));
            } else if ("VIP".equals(level)) {
                holder.tvLevel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F8D7DA")));
                holder.tvLevel.setTextColor(Color.parseColor("#721C24"));
            } else {
                holder.tvLevel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F6F4F8")));
                holder.tvLevel.setTextColor(Color.parseColor("#4A4650"));
            }

            // Points
            holder.tvPoints.setText(u.points + " điểm");

            // Checkbox state
            holder.cbSelect.setOnCheckedChangeListener(null);
            holder.cbSelect.setChecked(selectedUids.contains(u.uid));

            // Handle row click
            holder.itemView.setOnClickListener(v -> {
                boolean isChecked = !holder.cbSelect.isChecked();
                holder.cbSelect.setChecked(isChecked);
                if (isChecked) {
                    selectedUids.add(u.uid);
                } else {
                    selectedUids.remove(u.uid);
                }
                updateSelectCount();
                checkIfAllSelected();
            });
            
            // Handle checkbox click
            holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedUids.add(u.uid);
                } else {
                    selectedUids.remove(u.uid);
                }
                updateSelectCount();
                checkIfAllSelected();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvContact, tvLevel, tvPoints;
            CheckBox cbSelect;
            ImageView imgAvatar;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tvCustomerName);
                tvContact = itemView.findViewById(R.id.tvCustomerContact);
                tvLevel = itemView.findViewById(R.id.tvCustomerLevel);
                tvPoints = itemView.findViewById(R.id.tvCustomerPoints);
                cbSelect = itemView.findViewById(R.id.cbSelect);
                imgAvatar = itemView.findViewById(R.id.imgAvatar);
            }
        }
    }
}
