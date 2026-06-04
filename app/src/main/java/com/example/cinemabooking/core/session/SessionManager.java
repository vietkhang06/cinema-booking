package com.example.cinemabooking.core.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SessionManager {

    private static final String PREF_NAME          = "movie_booking_prefs";
    private static final String ENCRYPTED_PREF_NAME = "movie_booking_secure_prefs";
    private static final String KEY_IS_LOGGED_IN    = "is_logged_in";
    private static final String KEY_ROLE            = "role";
    private static final String KEY_UID             = "uid";
    private static final String KEY_REMEMBERED_EMAIL    = "remembered_email";
    private static final String KEY_REMEMBER_ME         = "remember_me";
    private static final String KEY_REMEMBERED_PASSWORD = "remembered_password";

    private final SharedPreferences sharedPreferences;
    /** SharedPreferences được mã hoá bằng AES-256 để lưu password an toàn */
    private SharedPreferences encryptedPrefs;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            encryptedPrefs = EncryptedSharedPreferences.create(
                    ENCRYPTED_PREF_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback: dùng SharedPreferences thường nếu thiết bị không hỗ trợ
            encryptedPrefs = sharedPreferences;
        }
    }

    public void saveLoginState(boolean isLoggedIn, String role, String uid) {
        sharedPreferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
                .putString(KEY_ROLE, role)
                .putString(KEY_UID, uid)
                .apply();
    }

    public boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
//        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, "customer");
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_UID, "");
    }

    public void saveRememberedEmail(String email) {
        sharedPreferences.edit()
                .putString(KEY_REMEMBERED_EMAIL, email)
                .apply();
    }

    public String getRememberedEmail() {
        return sharedPreferences.getString(KEY_REMEMBERED_EMAIL, "");
    }

    public void clearRememberedEmail() {
        sharedPreferences.edit()
                .remove(KEY_REMEMBERED_EMAIL)
                .apply();
    }

    /** Lưu password đã được mã hoá bằng EncryptedSharedPreferences */
    public void saveRememberedPassword(String password) {
        encryptedPrefs.edit()
                .putString(KEY_REMEMBERED_PASSWORD, password)
                .apply();
    }

    /** Đọc password đã lưu (trả về "" nếu chưa lưu) */
    public String getRememberedPassword() {
        return encryptedPrefs.getString(KEY_REMEMBERED_PASSWORD, "");
    }

    /** Xoá password đã lưu */
    public void clearRememberedPassword() {
        encryptedPrefs.edit()
                .remove(KEY_REMEMBERED_PASSWORD)
                .apply();
    }

    public void saveRememberMe(boolean rememberMe) {
        sharedPreferences.edit()
                .putBoolean(KEY_REMEMBER_ME, rememberMe)
                .apply();
    }

    public boolean isRememberMe() {
        return sharedPreferences.getBoolean(KEY_REMEMBER_ME, true);
    }

    public void logout() {
        sharedPreferences.edit()
                .remove(KEY_IS_LOGGED_IN)
                .remove(KEY_ROLE)
                .remove(KEY_UID)
                .remove(KEY_REMEMBER_ME)
                .apply();
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }
}