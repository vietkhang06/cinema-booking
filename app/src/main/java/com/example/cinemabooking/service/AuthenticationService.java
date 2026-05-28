package com.example.cinemabooking.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.cinemabooking.core.constants.UserRoles;
import com.example.cinemabooking.core.session.SessionManager;
import com.example.cinemabooking.data.repository.UserRepositoryImpl;
import com.example.cinemabooking.domain.common.AuthCallback;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.User;
import com.example.cinemabooking.domain.repository.UserRepository;
import com.facebook.AccessToken;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthenticationService {
    private final FirebaseAuth auth;
    private final SessionManager sessionManager;

    UserRepository userRepo;

    public AuthenticationService(Context context) {
        this.auth = FirebaseAuth.getInstance();
        this.sessionManager = new SessionManager(context);

        userRepo = new UserRepositoryImpl();
    }

    private User currentAuthUser;
    public void getCurrentAuthUser(ResultCallback<User> callback) {
        FirebaseUser fUser = auth.getCurrentUser();
        if (fUser == null) {
            callback.onSuccess(null);
            return;
        }

        if (currentAuthUser != null && currentAuthUser.uid.equals(fUser.getUid())) {
            callback.onSuccess(currentAuthUser);
            return;
        }

        userRepo.getUserById(fUser.getUid(), new ResultCallback<User>() {
            @Override
            public void onSuccess(User data) {
                currentAuthUser = data;
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    public void getCurrentAuthUser() {
        getCurrentAuthUser(new ResultCallback<User>() {
            @Override
            public void onSuccess(User data) {}

            @Override
            public void onError(String message) {}
        });
    }

    public User getCachedUser() {
        return currentAuthUser;
    }

    public void setCurrentAuthUser(User user) {
        this.currentAuthUser = user;
    }

    public void removeCurrentAuthUser(){
        currentAuthUser = null;
    }

    public void signInWithEmailAndPassword(
            @NonNull String email,
            @NonNull String password,
            boolean isRemember,
            AuthCallback callback
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                String uid = authResult.getUser().getUid();

                userRepo.getUserById(uid, new ResultCallback<User>() {
                    @Override
                    public void onSuccess(User data) {
                        if(data == null){
                            callback.onError("Không tìm thấy user.");
                            return;
                        }

                        sessionManager.saveLoginState(true, data.role, data.uid);
                        sessionManager.saveRememberMe(isRemember);
                        if (isRemember) {
                            sessionManager.saveRememberedEmail(email);
                        } else {
                            sessionManager.clearRememberedEmail();
                        }

                        callback.onSuccess(data);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }

                });
            })
            .addOnFailureListener(e -> {
                callback.onError(e.getMessage());
            });
    }

    public void signUpWithEmailAndPassword(
            @NonNull String email,
            @NonNull String password,
            @NonNull String phone,
            AuthCallback callback
    ) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser fUser = authResult.getUser();
                    if (fUser == null) {
                        callback.onError("User null");
                        return;
                    }

                    userRepo.createUser(newUserDoc(fUser, phone), new ResultCallback<User>() {
                        @Override
                        public void onSuccess(User data) {
                            if (data == null) {
                                callback.onError("Lỗi khởi tạo người dùng.");
                                return;
                            }

                            sessionManager.saveLoginState(true, data.role, data.uid);
                            sessionManager.saveRememberMe(true);
                            callback.onSuccess(data);
                        }

                        @Override
                        public void onError(String message) {
                            callback.onError(message);
                        }
                    });
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void handleFacebookAccessToken(AccessToken token, AuthCallback callback) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fUser = authResult.getUser();
                    if (fUser == null) {
                        callback.onError("Facebook user null");
                        return;
                    }

                    loadOrCreateUser(
                            fUser,
                            null,
                            new AuthCallback() {
                                @Override
                                public void onSuccess(User user) {
                                    sessionManager.saveLoginState(true, user.role, user.uid);
                                    sessionManager.saveRememberMe(true);
                                    callback.onSuccess(user);
                                }

                                @Override
                                public void onError(String message) {
                                    callback.onError(message);
                                }
                            }
                    );
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public void signInWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser fUser = authResult.getUser();
                    if (fUser == null) {
                        callback.onError("Google user null");
                        return;
                    }

                    loadOrCreateUser(
                            fUser,
                            null,
                            new AuthCallback() {
                                @Override
                                public void onSuccess(User user) {
                                    sessionManager.saveLoginState(true, user.role, user.uid);
                                    sessionManager.saveRememberMe(true);
                                    callback.onSuccess(user);
                                }

                                @Override
                                public void onError(String message) {
                                    callback.onError(message);
                                }
                            }
                    );
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    public Task<Void> forgetAndResetPassword(@NonNull String email) {
        return auth.sendPasswordResetEmail(email);
    }

    public Task<Void> updatePassword(String newPassword) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            return user.updatePassword(newPassword);
        }
        return com.google.android.gms.tasks.Tasks.forException(new Exception("User not authenticated"));
    }

    public void logOut() {
        auth.signOut();
        sessionManager.logout();
    }

    private void loadOrCreateUser(
            @NonNull FirebaseUser fUser,
            @Nullable String phone,
            @NonNull AuthCallback callback
    ) {
        userRepo.getUserById(fUser.getUid(), new ResultCallback<User>() {
            @Override
            public void onSuccess(User data) {
                if (data != null) {
                    callback.onSuccess(data);
                    return;
                }

                userRepo.createUser(newUserDoc(fUser, phone), new ResultCallback<User>() {
                    @Override
                    public void onSuccess(User created) {
                        if (created == null) {
                            callback.onError("Không tạo được user.");
                            return;
                        }
                        callback.onSuccess(created);
                    }

                    @Override
                    public void onError(String message) {
                        callback.onError(message);
                    }
                });
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    private User newUserDoc(@Nullable FirebaseUser fUser, @Nullable String phone) {
        if (fUser == null) return null;

        User user = new User();
        user.uid = fUser.getUid();
        user.email = fUser.getEmail();
        user.phone = phone;
        user.role = UserRoles.CUSTOMER;
        user.status = "active";
        user.memberLevel = "basic";
        user.createdAt = System.currentTimeMillis();
        user.updatedAt = System.currentTimeMillis();
        user.deleted = false;
        return user;
    }
}