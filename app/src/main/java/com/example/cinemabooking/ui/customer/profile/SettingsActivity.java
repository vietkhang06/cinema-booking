package com.example.cinemabookingapp.ui.customer.profile;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabookingapp.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private SwitchMaterial switchLocation, switchNotification;
    private android.view.View btnLanguage;
    private TextView tvCurrentLanguage, tvAppVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        bindActions();
        loadSettings();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        switchLocation = findViewById(R.id.switchLocation);
        switchNotification = findViewById(R.id.switchNotification);
        btnLanguage = findViewById(R.id.btnLanguage);
        tvCurrentLanguage = findViewById(R.id.tvCurrentLanguage);
        tvAppVersion = findViewById(R.id.tvAppVersion);
    }

    private void bindActions() {
        btnBack.setOnClickListener(v -> finish());

        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestLocationPermission();
            } else {
                Toast.makeText(this, "Đã tắt định vị", Toast.LENGTH_SHORT).show();
            }
        });

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Đã bật thông báo" : "Đã tắt thông báo";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        btnLanguage.setOnClickListener(v -> showLanguageDialog());
    }

    private void loadSettings() {
        // Mock loading current version
        tvAppVersion.setText("Galaxy Cinema phiên bản 3.6.11");
        
        // In a real app, you would load these from SharedPreferences
        switchNotification.setChecked(true);
        switchLocation.setChecked(false);
    }

    private void requestLocationPermission() {
        // Just a mock for now
        Toast.makeText(this, "Đang yêu cầu quyền truy cập vị trí...", Toast.LENGTH_SHORT).show();
        // Here you would typically use ActivityResultLauncher for permissions
    }

    private void showLanguageDialog() {
        String[] languages = {"Tiếng Việt", "English"};
        int selectedIndex = tvCurrentLanguage.getText().toString().equals("Tiếng Việt") ? 0 : 1;

        new AlertDialog.Builder(this)
                .setTitle("Chọn ngôn ngữ")
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String selected = languages[which];
                    tvCurrentLanguage.setText(selected);
                    Toast.makeText(this, "Đã đổi sang " + selected, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    // In a real app, you would update locale and recreate activity
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
