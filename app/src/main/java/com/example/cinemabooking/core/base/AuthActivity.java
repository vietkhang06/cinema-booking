package com.example.cinemabooking.core.base;

import com.example.cinemabooking.core.navigation.AppNavigator;
import com.example.cinemabooking.di.ServiceProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AuthActivity extends BaseActivity{
    FirebaseAuth.AuthStateListener authStateListener;
    // ZELIOUS TASK: Lớp Base Activity dùng để bảo vệ các màn hình yêu cầu đăng nhập.
    // Đăng ký AuthStateListener toàn cục. Nếu user bị null (mất session do Auth hết hạn hoặc đăng xuất), tự động đá văng về màn hình Login.
    @Override
    protected void onStart() {
        super.onStart();
        authStateListener = authState -> {
            FirebaseUser user = authState.getCurrentUser();
            ServiceProvider.getInstance().getAuthenticationService().getCurrentAuthUser();
            if (user == null) {
                ServiceProvider.getInstance().getAuthenticationService().removeCurrentAuthUser();
                AppNavigator.goToLogin(this);
            }
        };

        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener != null)
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
    }
}