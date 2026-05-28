package com.example.cinemabooking.ui.staff;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.AuthActivity;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.InputStream;

public class StaffScanQRActivity extends AuthActivity {

    private TextView tvScanResult, tvTicketStatus;
    private MaterialButton btnScanCamera, btnUploadImage;
    private View backBtn;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    decodeQRFromUri(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_scan_qr);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        bindActions();
    }

    private void initViews() {
        tvScanResult = findViewById(R.id.tv_scan_result);
        tvTicketStatus = findViewById(R.id.tv_ticket_status);
        btnScanCamera = findViewById(R.id.btn_scan_camera);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        backBtn = findViewById(R.id.back_btn);
    }

    private void bindActions() {
        backBtn.setOnClickListener(v -> finish());
        btnScanCamera.setOnClickListener(v -> openGoogleScanner());
        btnUploadImage.setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });
    }

    private void openGoogleScanner() {
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .enableAutoZoom()
                .build();

        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this, options);

        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    String rawValue = barcode.getRawValue();
                    if (rawValue != null && !rawValue.trim().isEmpty()) {
                        tvScanResult.setText(rawValue);
                        tvTicketStatus.setText("Trạng thái vé: Hợp lệ (Camera)");
                        
                        Intent intent = new Intent(this, StaffInvoiceActivity.class);
                        intent.putExtra("invoiceId", rawValue);
                        startActivity(intent);
                    } else {
                        showToast("Mã QR rỗng");
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Quét thất bại: " + e.getMessage());
                });
    }

    private void decodeQRFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) {
                showToast("Không thể tải ảnh");
                return;
            }

            int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

            LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            Reader reader = new QRCodeReader();
            Result result = reader.decode(binaryBitmap);

            String qrText = result.getText();
            if (qrText != null && !qrText.trim().isEmpty()) {
                tvScanResult.setText(qrText);
                tvTicketStatus.setText("Trạng thái vé: Hợp lệ (Thư viện)");
                
                Intent intent = new Intent(this, StaffInvoiceActivity.class);
                intent.putExtra("invoiceId", qrText);
                startActivity(intent);
            } else {
                showToast("Mã QR rỗng");
            }
        } catch (Exception e) {
            tvScanResult.setText("Lỗi đọc mã QR");
            tvTicketStatus.setText("Trạng thái vé: Không hợp lệ hoặc không có QR");
            showToast("Không tìm thấy mã QR trong ảnh");
        }
    }
}
