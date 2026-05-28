package com.example.cinemabooking.di;

import android.content.Context;

import com.example.cinemabooking.service.AuthenticationService;
import com.example.cinemabooking.service.ProfileService;
import com.example.cinemabooking.service.InvoiceService;
import com.example.cinemabooking.service.ProfileService;
import com.example.cinemabooking.service.UploadService;

public class ServiceProvider {

    private static ServiceProvider instance;
    private final Context appContext;

    private AuthenticationService authenticationService;
    private ProfileService profileService;
    private UploadService uploadService;
    private com.example.cinemabooking.service.BookingService bookingService;

    private ServiceProvider(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static synchronized ServiceProvider getInstance(Context context) {
        if (instance == null) {
            instance = new ServiceProvider(context);
        }
        return instance;
    }

    public static synchronized ServiceProvider getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ServiceProvider chưa được init!");
        }
        return instance;
    }

    public AuthenticationService getAuthenticationService() {
        if (authenticationService == null) {
            authenticationService = new AuthenticationService(appContext);
        }
        return authenticationService;
    }

    public ProfileService getProfileService() {
        if (profileService == null) {
            profileService = new ProfileService();
        }
        return profileService;
    }

    public UploadService getUploadService() {
        if (uploadService == null) {
            uploadService = new UploadService(appContext);
        }
        return uploadService;
    }

    public com.example.cinemabooking.service.BookingService getBookingService() {
        if (bookingService == null) {
            bookingService = new com.example.cinemabooking.service.BookingService();
        }
        return bookingService;
    }

    InvoiceService invoiceService;

    public InvoiceService getInvoiceService() {
        if (invoiceService == null) {
            invoiceService = new InvoiceService();
        }
        return invoiceService;
    }
}