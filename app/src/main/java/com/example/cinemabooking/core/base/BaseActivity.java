package com.example.cinemabooking.core.base;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cinemabooking.MyApp;
import com.example.cinemabooking.core.navigation.AppNavigator;
import com.example.cinemabooking.core.session.SessionManager;
import com.example.cinemabooking.di.AppContainer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public abstract class BaseActivity extends AppCompatActivity {

    protected AppContainer appContainer;
    protected SessionManager sessionManager;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContainer = ((MyApp) getApplication()).getAppContainer();
        sessionManager = appContainer.getSessionManager();
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void showLoading(boolean isLoading) {
        if (isLoading) {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Đang tải...");
                progressDialog.setCancelable(false);
            }
            progressDialog.show();
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }
}