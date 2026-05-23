package com.example.cinemabooking.core.base;

import com.example.cinemabooking.core.navigation.AppNavigator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AuthActivity extends BaseActivity{
    FirebaseAuth.AuthStateListener authStateListener;
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