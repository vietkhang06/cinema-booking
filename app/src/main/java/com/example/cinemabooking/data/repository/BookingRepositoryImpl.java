package com.example.cinemabooking.data.repository;

import com.example.cinemabooking.data.dto.ApiResponse;
import com.example.cinemabooking.data.dto.BookingDTO;
import com.example.cinemabooking.data.mapper.BookingMapper;
import com.example.cinemabooking.data.remote.api.BookingApiService;
import com.example.cinemabooking.data.remote.api.RetrofitClient;
import com.example.cinemabooking.data.remote.datasource.BookingRemoteDataSource;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Booking;
import com.example.cinemabooking.domain.repository.BookingRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingRepositoryImpl implements BookingRepository {
    private final BookingRemoteDataSource remoteDataSource;
    private final BookingApiService bookingApiService;

    public BookingRepositoryImpl() {
        this.remoteDataSource = new BookingRemoteDataSource();
        this.bookingApiService = RetrofitClient.getInstance().create(BookingApiService.class);
    }

    public BookingRepositoryImpl(BookingRemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.bookingApiService = RetrofitClient.getInstance().create(BookingApiService.class);
    }

    @Override
    public void createBooking(Booking booking, ResultCallback<Booking> callback) {
        if (callback != null) callback.onError("Tính năng tạo vé mới đang được bảo trì.");
    }

    @Override
    public void getBookingById(String bookingId, ResultCallback<Booking> callback) {
        // Thử REST API trước — chuẩn và đồng bộ nhất
        bookingApiService.getBookingById(bookingId).enqueue(new Callback<ApiResponse<BookingDTO>>() {
            @Override
            public void onResponse(Call<ApiResponse<BookingDTO>> call, Response<ApiResponse<BookingDTO>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    Booking booking = BookingMapper.toDomain(response.body().getData());
                    if (callback != null) callback.onSuccess(booking);
                } else {
                    // Fallback: Firestore
                    getBookingByIdFromFirestore(bookingId, callback);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BookingDTO>> call, Throwable t) {
                // Fallback: Firestore
                getBookingByIdFromFirestore(bookingId, callback);
            }
        });
    }

    private void getBookingByIdFromFirestore(String bookingId, ResultCallback<Booking> callback) {
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
        // Gọi REST API /bookings/my — dùng auth token, đảm bảo đúng user, đồng bộ nhất
        // userId param được ignore vì API dùng token
        bookingApiService.getMyBookings().enqueue(new Callback<ApiResponse<List<BookingDTO>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<BookingDTO>>> call, Response<ApiResponse<List<BookingDTO>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<BookingDTO> dtos = response.body().getData();
                    List<Booking> bookings = new ArrayList<>();
                    for (BookingDTO dto : dtos) {
                        Booking b = BookingMapper.toDomain(dto);
                        if (b != null && !b.deleted) {
                            bookings.add(b);
                        }
                    }
                    // Sắp xếp theo ngày tạo mới nhất lên đầu
                    bookings.sort((b1, b2) -> Long.compare(b2.createdAt, b1.createdAt));
                    if (callback != null) callback.onSuccess(bookings);
                } else {
                    // Fallback: Firestore nếu API lỗi (token hết hạn, mạng, ...)
                    android.util.Log.w("BOOKING_REPO", "REST API failed (code=" + response.code() + "), falling back to Firestore");
                    getBookingsByUserIdFromFirestore(callback);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<BookingDTO>>> call, Throwable t) {
                android.util.Log.w("BOOKING_REPO", "REST API network error, falling back to Firestore: " + t.getMessage());
                getBookingsByUserIdFromFirestore(callback);
            }
        });
    }

    private void getBookingsByUserIdFromFirestore(ResultCallback<List<Booking>> callback) {
        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid()
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
                        try {
                            Booking booking = doc.toObject(Booking.class);
                            if (booking != null) {
                                booking.bookingId = doc.getId();
                                bookings.add(booking);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("BookingRepositoryImpl", "Lỗi phân tích tài liệu đặt vé: " + doc.getId(), e);
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
