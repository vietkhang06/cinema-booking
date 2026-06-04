package com.example.cinemabooking.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class UploadService {
    private final Context context;

    public UploadService(Context context) {
        this.context = context;
    }

    /**
     * Chuyển đổi ảnh sang chuỗi Base64 để lưu trực tiếp vào Firestore (Miễn phí)
     */
    public String uploadImage(Uri uri) {
        if (uri == null) return null;

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            // 1. Đọc ảnh từ Uri
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (originalBitmap == null) return null;

            // 2. Thu nhỏ ảnh (Resize) để tiết kiệm dung lượng Firestore (Max 1MB per document)
            // Chúng ta sẽ thu nhỏ về tối đa 200px chiều rộng/cao cho ảnh đại diện
            int size = 200;
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, size, 
                (int) (originalBitmap.getHeight() * ((float) size / originalBitmap.getWidth())), true);

            // 3. Nén ảnh (Compress) sang JPEG chất lượng 70%
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // 4. Chuyển sang chuỗi Base64 với tiền tố data:image/jpeg;base64,
            String base64Image = Base64.encodeToString(byteArray, Base64.NO_WRAP);
            
            // Giải phóng bộ nhớ
            originalBitmap.recycle();
            scaledBitmap.recycle();

            Log.d("UploadService", "Converted image to Base64 (Size: " + base64Image.length() + " chars)");
            
            // Trả về chuỗi định dạng Data URI để Glide hiển thị được ngay
            return "data:image/jpeg;base64," + base64Image;

        } catch (Exception e) {
            Log.e("UploadService", "Error converting image to Base64: " + e.getMessage());
            return null;
        }
    }
}
