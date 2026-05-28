package com.example.cinemabooking.utils;

import android.os.Build;

import androidx.annotation.NonNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeConverter {

    @NonNull
    public static String convertToDateTimeString(long timeStamp){
        DateTimeFormatter formatter;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneId.systemDefault()); // Or ZoneId.of("UTC")
            Instant instant = Instant.ofEpochMilli(timeStamp);
            String formattedDate = formatter.format(instant);
            return formattedDate;
        }
        return "";
    }
}
