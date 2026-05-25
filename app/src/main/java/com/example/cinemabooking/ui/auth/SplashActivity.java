package com.example.cinemabookingapp.ui.auth;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.cinemabookingapp.R;
import com.example.cinemabookingapp.core.base.BaseActivity;
import com.example.cinemabookingapp.core.navigation.AppNavigator;

public class SplashActivity extends BaseActivity {

    private static final long SPLASH_DELAY = 2500L;

    private View[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initDots();
        startDotAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (sessionManager.isLoggedIn()) {
                AppNavigator.goToHomeByRole(this, sessionManager.getRole());
            } else {
                AppNavigator.goToLogin(this);
            }
        }, SPLASH_DELAY);
    }

    private void initDots() {
        dots = new View[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3),
                findViewById(R.id.dot4),
                findViewById(R.id.dot5),
                findViewById(R.id.dot6),
                findViewById(R.id.dot7)
        };
    }

    private void startDotAnimation() {
        long delayStep = 120L;

        for (int i = 0; i < dots.length; i++) {
            View dot = dots[i];
            dot.setScaleX(1f);
            dot.setScaleY(1f);

            ObjectAnimator animator = ObjectAnimator.ofFloat(dot, "translationY", 0f, -10f, 0f);
            animator.setDuration(500L);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setStartDelay(i * delayStep);
            animator.start();
        }
    }
}