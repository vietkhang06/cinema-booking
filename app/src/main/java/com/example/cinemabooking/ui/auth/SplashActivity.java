package com.example.cinemabooking.ui.auth;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.cinemabooking.R;
import com.example.cinemabooking.core.base.BaseActivity;
import com.example.cinemabooking.core.navigation.AppNavigator;
import com.example.cinemabooking.di.ServiceProvider;

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
            if (sessionManager.isLoggedIn() && sessionManager.isRememberMe()) {
                AppNavigator.goToHomeByRole(this, sessionManager.getRole());
            } else {
                ServiceProvider.getInstance(getApplicationContext()).getAuthenticationService().logOut();
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