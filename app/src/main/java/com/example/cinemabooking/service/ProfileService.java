package com.example.cinemabooking.service;

import com.example.cinemabooking.core.constants.FirestoreCollections;
import com.example.cinemabooking.data.repository.UserRepositoryImpl;
import com.example.cinemabooking.di.ServiceProvider;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Booking;
import com.example.cinemabooking.domain.model.User;
import com.example.cinemabooking.domain.repository.BookingRepository;
import com.example.cinemabooking.domain.repository.UserRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.stream.Collectors;

public class ProfileService {

    AuthenticationService authService;
    UserRepository userRepo;
    FirebaseFirestore firestore;

    public ProfileService(){
        authService = ServiceProvider.getInstance().getAuthenticationService();
        userRepo = new UserRepositoryImpl();
        firestore = FirebaseFirestore.getInstance();
    }
    public void getUserProfile(ResultCallback<User> callback) {
        authService.getCurrentAuthUser(callback);
    }

    public User getCachedProfile() {
        return authService.getCachedUser();
    }

    public void updateUserProfile(User user, ResultCallback<User> callback){
        userRepo.updateUser(user, new ResultCallback<User>() {
            @Override
            public void onSuccess(User data) {
                authService.setCurrentAuthUser(data);
                if (callback != null) callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                if (callback != null) callback.onError(message);
            }
        });
    }

    public void getUserTotalSpending(ResultCallback<Double> callback){
        com.google.firebase.auth.FirebaseUser fUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        String uid = null;
        if (fUser != null) {
            uid = fUser.getUid();
        } else {
            User cached = getCachedProfile();
            if (cached != null) {
                uid = cached.uid;
            }
        }

        if (uid == null) {
            if (callback != null) callback.onSuccess(0.0);
            return;
        }

        final String userId = uid;

        firestore.collection(FirestoreCollections.BOOKINGS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(bookingSnapshot -> {
                    double movieTotal = bookingSnapshot.toObjects(Booking.class).stream()
                            .filter(b -> b.bookingStatus != null && 
                                    ("confirmed".equalsIgnoreCase(b.bookingStatus) || "success".equalsIgnoreCase(b.bookingStatus)))
                            .mapToDouble(b -> b.total)
                            .sum();

                    firestore.collection("cine_shop_orders")
                            .whereEqualTo("userId", userId)
                            .get()
                            .addOnSuccessListener(shopSnapshot -> {
                                double shopTotal = 0.0;
                                for (com.google.firebase.firestore.DocumentSnapshot doc : shopSnapshot.getDocuments()) {
                                    String status = doc.getString("status");
                                    if (status != null && ("confirmed".equalsIgnoreCase(status) || "success".equalsIgnoreCase(status))) {
                                        Double price = doc.getDouble("totalPrice");
                                        if (price != null) {
                                            shopTotal += price;
                                        }
                                    }
                                }
                                if (callback != null) callback.onSuccess(movieTotal + shopTotal);
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onSuccess(movieTotal);
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onSuccess(0.0);
                });
    }
}
