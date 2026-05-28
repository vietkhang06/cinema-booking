package com.example.cinemabooking.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRCodeGenerator {
    public static final int QR_SIZE = 200;
    public static Bitmap generateQRCodeFromString(String content) throws WriterException{
        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        BitMatrix matrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
        Bitmap qrBitmap = Bitmap.createBitmap(
                QR_SIZE,
                QR_SIZE,
                Bitmap.Config.RGB_565);

        for(int x = 0;  x < QR_SIZE; x++){
            for(int y = 0; y< QR_SIZE; y++){
                qrBitmap.setPixel(x, y, matrix.get(x,y) ? Color.BLACK : Color.WHITE);
            }
        }
        
        return qrBitmap;
    }
}
