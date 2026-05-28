package com.example.cinemabooking.service;

import android.util.Log;

import androidx.annotation.Nullable;

import com.example.cinemabooking.core.constants.FirestoreCollections;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Booking;
import com.example.cinemabooking.domain.model.Movie;
import com.example.cinemabooking.domain.model.Showtime;
import com.example.cinemabooking.domain.model.Snack;
import com.example.cinemabooking.domain.model.SnackOrder;
import com.example.cinemabooking.domain.model.SnackOrderItem;
import com.example.cinemabooking.domain.repository.MovieRepository;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.lang.Record;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InvoiceService {
    public class InvoiceDetail{
        public Booking booking;
        public Showtime showtime;
        public SnackOrder snackOrder;

        // snack name,price,quantity
        public List<String> snackItems;
        public Movie movie;


    }
    public static final String logTag = "Invoice Service";
    FirebaseFirestore firestore;
    MovieRepository movieRepository;
    public InvoiceService(){
        firestore = FirebaseFirestore.getInstance();
    }

    public void updatePaymentStatus(Booking booking, ResultCallback<Void> resultCallback){

        firestore.collection(FirestoreCollections.BOOKINGS)
                .document(booking.bookingId)
                .set(booking, SetOptions.mergeFields("paymentStatus", "paymentMethod", ""))
                .addOnSuccessListener(unused -> {
                    resultCallback.onSuccess(unused);
                })
                .addOnFailureListener(e -> {
                    resultCallback.onError(e.getMessage());
                });
    }

    public void getInvoiceFromId(String invoiceId, ResultCallback<Booking> callback){
        firestore.collection(FirestoreCollections.BOOKINGS)
                .document(invoiceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    callback.onSuccess(documentSnapshot.toObject(Booking.class));
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    @Nullable
    public InvoiceDetail getInvoiceDetail(String invoiceId){
        try {
            InvoiceDetail invoiceDetail = new InvoiceDetail();

            invoiceDetail.booking = Tasks.await(firestore.collection(FirestoreCollections.BOOKINGS).document(invoiceId).get()).toObject(Booking.class);

            Tasks.await(Tasks.whenAll(
                firestore.collection(FirestoreCollections.SHOWTIMES).document(invoiceDetail.booking.showtimeId).get()
                        .addOnSuccessListener(documentSnapshot ->
                                invoiceDetail.showtime = documentSnapshot.toObject(Showtime.class)
                        ),
                firestore.collection(FirestoreCollections.SNACK_ORDERS).document(invoiceDetail.booking.snackOrderId).get()
                        .addOnSuccessListener(documentSnapshot ->
                                invoiceDetail.snackOrder = documentSnapshot.toObject(SnackOrder.class)
                        )
            ));

            Tasks.await(Tasks.whenAllComplete(
                invoiceDetail.snackOrder.items.stream().filter(snackOrderItem -> snackOrderItem.snackId != null && !snackOrderItem.snackId.isBlank())
                        .map(snackOrderItem ->
                            firestore.collection(FirestoreCollections.SNACKS).document(snackOrderItem.snackId).get())
                        .collect(Collectors.toList())
            ).addOnSuccessListener((tasks) -> {
                invoiceDetail.snackItems = tasks.stream()
                        .filter(task -> task.isSuccessful())
                        .map(task -> {
                            DocumentSnapshot documentSnapshot = (DocumentSnapshot) task.getResult();
                            Snack snack = documentSnapshot.toObject(Snack.class);
                            return String.format("%s,%s,", snack.name, snack.price);
                        })
                        .collect(Collectors.toList());

                for(int i = 0; i < invoiceDetail.snackOrder.items.size(); i++){
                    invoiceDetail.snackItems.set(i, invoiceDetail.snackItems.get(i) + invoiceDetail.snackOrder.items.get(i).quantity);
                }
            }));

            invoiceDetail.movie = Tasks.await(firestore.collection(FirestoreCollections.MOVIES).document(invoiceDetail.showtime.movieId).get()).toObject(Movie.class);

            return invoiceDetail;
        }catch (Exception e){
            Log.e(logTag, e.getMessage());
            return null;
        }
    }

}
