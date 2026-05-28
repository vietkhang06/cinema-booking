package com.example.cinemabooking.data.remote.api;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder requestBuilder = chain.request().newBuilder();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            try {
                // Synchronously get ID Token with timeout to prevent app hang
                Task<GetTokenResult> task = user.getIdToken(false);
                GetTokenResult tokenResult = Tasks.await(task, 10, java.util.concurrent.TimeUnit.SECONDS);
                String token = tokenResult.getToken();
                
                if (token != null) {
                    requestBuilder.addHeader("Authorization", "Bearer " + token);
                } else {
                    throw new IOException("Retrieve Firebase Token failed: token is null");
                }
            } catch (Exception e) {
                android.util.Log.w("AuthInterceptor", "Standard token fetch failed: " + e.getMessage() + ". Attempting force refresh...");
                try {
                    Task<GetTokenResult> task = user.getIdToken(true); // Force refresh
                    GetTokenResult tokenResult = Tasks.await(task, 10, java.util.concurrent.TimeUnit.SECONDS);
                    String token = tokenResult.getToken();
                    if (token != null) {
                        requestBuilder.addHeader("Authorization", "Bearer " + token);
                    } else {
                        throw new IOException("Firebase token empty after force refresh");
                    }
                } catch (Exception ex) {
                    throw new IOException("Authentication error: Session expired or invalid. Please log in again.", ex);
                }
            }
        } else {
            String path = chain.request().url().encodedPath();
            if (path.contains("/profile") || path.contains("/user") || path.contains("/bookings") || path.contains("/payment") || path.contains("/seats/lock") || path.contains("/seats/release")) {
                throw new IOException("Access denied: User is not authenticated for " + path);
            }
        }
        return chain.proceed(requestBuilder.build());
    }
}
