package com.example.cinemabooking.ui.customer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class BookingTimerManager {

    private static final String PREFS_NAME = "booking_timer_prefs";
    private static final String KEY_END_TIME = "end_time_millis";
    private static final String KEY_IS_RUNNING = "is_running";

    private static BookingTimerManager instance;

    private long endTimeMillis = 0;
    private boolean isRunning = false;
    private CountDownTimer countDownTimer;
    private final List<TimerListener> listeners = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface TimerListener {
        void onTick(long millisUntilFinished);
        void onFinish();
    }

    private BookingTimerManager() {}

    public static synchronized BookingTimerManager getInstance() {
        if (instance == null) {
            instance = new BookingTimerManager();
        }
        return instance;
    }

    public synchronized void startTimer(Context context, long durationMillis) {
        // Load existing timer if already running to prevent double starting
        if (isTimerActive(context)) {
            restoreTimer(context);
            return;
        }

        endTimeMillis = System.currentTimeMillis() + durationMillis;
        isRunning = true;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putLong(KEY_END_TIME, endTimeMillis)
                .putBoolean(KEY_IS_RUNNING, true)
                .apply();

        startInternalTimer();
    }

    public synchronized void restoreTimer(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        endTimeMillis = prefs.getLong(KEY_END_TIME, 0);
        isRunning = prefs.getBoolean(KEY_IS_RUNNING, false);

        if (isRunning && endTimeMillis > System.currentTimeMillis()) {
            startInternalTimer();
        } else {
            stopTimer(context);
        }
    }

    public synchronized void stopTimer(Context context) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        isRunning = false;
        endTimeMillis = 0;

        prefs.edit()
                .remove(KEY_END_TIME)
                .remove(KEY_IS_RUNNING)
                .apply();
    }

    public synchronized boolean isTimerActive(Context context) {
        if (isRunning && endTimeMillis > System.currentTimeMillis()) {
            return true;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long savedEndTime = prefs.getLong(KEY_END_TIME, 0);
        boolean savedIsRunning = prefs.getBoolean(KEY_IS_RUNNING, false);
        return savedIsRunning && savedEndTime > System.currentTimeMillis();
    }

    public synchronized long getRemainingTimeMillis() {
        if (!isRunning) return 0;
        long remaining = endTimeMillis - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public synchronized void registerListener(TimerListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        // Send initial tick immediately if running
        if (isRunning) {
            long remaining = getRemainingTimeMillis();
            listener.onTick(remaining);
        }
    }

    public synchronized void unregisterListener(TimerListener listener) {
        listeners.remove(listener);
    }

    private synchronized void startInternalTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        long remaining = getRemainingTimeMillis();
        if (remaining <= 0) {
            notifyFinished();
            return;
        }

        countDownTimer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                notifyTick(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                notifyFinished();
            }
        };
        countDownTimer.start();
    }

    private synchronized void notifyTick(long millisUntilFinished) {
        mainHandler.post(() -> {
            synchronized (BookingTimerManager.this) {
                for (TimerListener listener : new ArrayList<>(listeners)) {
                    listener.onTick(millisUntilFinished);
                }
            }
        });
    }

    private synchronized void notifyFinished() {
        isRunning = false;
        endTimeMillis = 0;
        mainHandler.post(() -> {
            synchronized (BookingTimerManager.this) {
                for (TimerListener listener : new ArrayList<>(listeners)) {
                    listener.onFinish();
                }
            }
        });
    }
}
