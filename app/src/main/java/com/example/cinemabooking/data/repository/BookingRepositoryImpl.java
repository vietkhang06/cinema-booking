package com.example.cinemabooking.data.repository;

import com.example.cinemabooking.data.remote.datasource.BookingRemoteDataSource;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Booking;
import com.example.cinemabooking.domain.repository.BookingRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class BookingRepositoryImpl implements BookingRepository {
    private final BookingRemoteDataSource remoteDataSource;

    public BookingRepositoryImpl() {
        this.remoteDataSource = new BookingRemoteDataSource();
    }

    public BookingRepositoryImpl(BookingRemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public void createBooking(Booking booking, ResultCallback<Booking> callback) {
        // Bookings are created directly via confirmBooking in BookingConfirmActivity
        if (callback != null) callback.onError("Tính năng tạo vé mới đang được bảo trì.");
    }

    @Override
    public void getBookingById(String bookingId, ResultCallback<Booking> callback) {
        FirebaseFirestore.getInstance()
                .collection("bookings")
                .document(bookingId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Booking booking = documentSnapshot.toObject(Booking.class);
                        if (booking != null) {
                            booking.bookingId = documentSnapshot.getId();
                            if (callback != null) callback.onSuccess(booking);
                        } else {
                            if (callback != null) callback.onError("Không thể phân tích dữ liệu vé.");
                        }
                    } else {
                        if (callback != null) callback.onError("Không tìm thấy vé.");
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getBookingsByUserId(String userId, ResultCallback<List<Booking>> callback) {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (currentUid == null) {
            if (callback != null) callback.onError("Người dùng chưa đăng nhập.");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("bookings")
                .whereEqualTo("userId", currentUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Booking> bookings = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Booking booking = doc.toObject(Booking.class);
                        if (booking != null) {
                            booking.bookingId = doc.getId();
                            bookings.add(booking);
                        }
                    }
                    // Sắp xếp theo ngày tạo mới nhất lên đầu
                    bookings.sort((b1, b2) -> Long.compare(b2.createdAt, b1.createdAt));
                    if (callback != null) callback.onSuccess(bookings);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void getBookingsByShowtimeId(String showtimeId, ResultCallback<List<Booking>> callback) {
        if (callback != null) callback.onError("Tính năng chưa được hỗ trợ.");
    }

    @Override
    public void getAllBookings(ResultCallback<List<Booking>> callback) {
        FirebaseFirestore.getInstance()
                .collection("bookings")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Booking> bookings = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Booking booking = doc.toObject(Booking.class);
                        if (booking != null) {
                            booking.bookingId = doc.getId();
                            bookings.add(booking);
                        }
                    }
                    if (callback != null) callback.onSuccess(bookings);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    @Override
    public void updateBookingStatus(String bookingId, String status, ResultCallback<Booking> callback) {
        if (callback != null) callback.onError("Tính năng cập nhật trạng thái chưa được hỗ trợ.");
    }

    @Override
    public void cancelBooking(String bookingId, ResultCallback<Booking> callback) {
        if (callback != null) callback.onError("Tính năng hủy vé chưa được hỗ trợ.");
    }

    @Override
    public void softDeleteBooking(String bookingId, ResultCallback<Void> callback) {
        if (callback != null) callback.onError("Tính năng xóa vé chưa được hỗ trợ.");
    }
}
