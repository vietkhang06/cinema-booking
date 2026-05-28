package com.example.cinemabooking;

import android.app.Application;

import com.example.cinemabooking.di.AppContainer;
import com.example.cinemabooking.di.ServiceProvider;
import com.google.firebase.FirebaseApp;

public class MyApp extends Application {

    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer(this);
        FirebaseApp.initializeApp(this);

        ServiceProvider.getInstance(getApplicationContext()).getAuthenticationService().getCurrentAuthUser();
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }
}