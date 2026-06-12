package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.*;
import com.cinemabooking.backend.dto.request.SeatBookingRequestDTO;
import com.cinemabooking.backend.service.BookingService;
import com.cinemabooking.backend.service.MovieService;
import com.cinemabooking.backend.service.ShowtimeService;
import com.cinemabooking.backend.service.UserService;
import com.cinemabooking.backend.payment.service.PaymentService;
import com.google.cloud.firestore.Firestore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Endpoints for advertising booking")
public class BookingController {

    private static final Logger log = LoggerFactory.getLogger(BookingController.class);

    @Autowired private Firestore firestore;

    @Autowired private BookingService bookingService;
    @Autowired private ShowtimeService showtimeService;
    @Autowired private UserService userService;
    @Autowired private MovieService movieService;
    @Autowired private PaymentService paymentService;

    @GetMapping("{id}")
    @Operation(summary = "Get booking detail by id")
    public ApiResponse<BookingDTO> getBookingDetailById(@PathVariable("id") String bookingId) throws ExecutionException, InterruptedException {
        BookingDTO booking = bookingService.getBookingById(bookingId);
        if(booking == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found");

        ShowtimeDTO showTime = showtimeService.getShowtimeById(booking.getShowtimeId());
        UserDTO user = userService.getUserById(booking.getUserId());

        booking.setShowtime(showTime);
        booking.setUser(user);

        return ApiResponse.<BookingDTO>builder()
                .success(true)
                .message("Booking fetched successfully")
                .data(booking)
                .build();
    }

    @GetMapping("/pending")
    @Operation(summary = "Get active pending booking for a user by showtime")
    public ResponseEntity<ApiResponse<BookingDTO>> getPendingActiveBooking(
            @AuthenticationPrincipal String userId,
            @RequestParam("showtimeId") String showtimeId
    ) throws ExecutionException, InterruptedException {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        BookingDTO booking = bookingService.getPendingActiveBooking(userId, showtimeId);
        return ResponseEntity.ok(
                ApiResponse.<BookingDTO>builder()
                        .success(booking != null)
                        .message(booking != null ? "Tìm thấy booking PENDING" : "Không có booking PENDING")
                        .data(booking)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @AuthenticationPrincipal String userId,
            @RequestBody SeatBookingRequestDTO bookingRequest
    ) throws ExecutionException, InterruptedException {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }

        BookingDTO bookingDTO = bookingService.processBookingCreation(userId, bookingRequest);

        // Kiểm tra xem đã có payment cho bookingId này chưa để tránh tạo trùng hóa đơn
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> existingPayments = firestore.collection("payments")
                    .whereEqualTo("bookingId", bookingDTO.getBookingId())
                    .get()
                    .get()
                    .getDocuments();
            if (existingPayments.isEmpty()) {
                paymentService.createPendingPayment(
                        bookingDTO.getBookingId(),
                        bookingDTO.getUserId(),
                        bookingDTO.getPaymentMethod(),
                        bookingDTO.getTotal()
                );
            }
        } catch (Exception e) {
            log.error("Failed to check or create payment for bookingId: " + bookingDTO.getBookingId(), e);
        }

        if ("cash".equalsIgnoreCase(bookingDTO.getPaymentMethod())) {
            bookingService.confirmBookingSeats(bookingDTO.getBookingId());
        }

        return ResponseEntity.ok(
                ApiResponse.<BookingDTO>builder()
                        .success(true)
                        .message("Booking fetched successfully")
                        .data(bookingDTO)
                        .build()
        );
    }

    @PutMapping("/payment/{id}/confirmed")
    public ResponseEntity<ApiResponse<?>> updatePaymentStatus(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") String bookingId
    ) throws ExecutionException, InterruptedException {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        BookingDTO booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy vé đặt.");
        }
        if (!userId.equals(booking.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xác nhận vé này.");
        }
        bookingService.updatePaymentStatus(bookingId, "SUCCESS", "CONFIRMED");
        bookingService.confirmBookingSeats(bookingId);

        // Also update payments document status to SUCCESS
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> payments = firestore.collection("payments")
                    .whereEqualTo("bookingId", bookingId)
                    .get()
                    .get()
                    .getDocuments();
            for (com.google.cloud.firestore.QueryDocumentSnapshot paymentDoc : payments) {
                firestore.collection("payments").document(paymentDoc.getId())
                        .update("status", "SUCCESS", "updatedAt", System.currentTimeMillis())
                        .get();
            }
        } catch (Exception e) {
            log.error("Failed to update payment status to SUCCESS for bookingId: " + bookingId, e);
        }

        return ResponseEntity.ok(
                ApiResponse.<BookingDTO>builder()
                        .success(true)
                        .message("Payment confirmed and seats booked successfully")
                        .build()
        );
    }

    @PutMapping("/payment/{id}/failed")
    public ResponseEntity<ApiResponse<?>> cancelBooking(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") String bookingId
    ) throws ExecutionException, InterruptedException {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        BookingDTO booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy vé đặt.");
        }
        if (!userId.equals(booking.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền hủy vé này.");
        }
        bookingService.updatePaymentStatus(bookingId, "FAILED", "CANCELLED");
        bookingService.releaseBookingSeats(bookingId);

        // Also update payments document status to FAILED
        try {
            List<com.google.cloud.firestore.QueryDocumentSnapshot> payments = firestore.collection("payments")
                    .whereEqualTo("bookingId", bookingId)
                    .get()
                    .get()
                    .getDocuments();
            for (com.google.cloud.firestore.QueryDocumentSnapshot paymentDoc : payments) {
                firestore.collection("payments").document(paymentDoc.getId())
                        .update("status", "FAILED", "updatedAt", System.currentTimeMillis())
                        .get();
            }
        } catch (Exception e) {
            log.error("Failed to update payment status to FAILED for bookingId: " + bookingId, e);
        }

        return ResponseEntity.ok(
                ApiResponse.<BookingDTO>builder()
                        .success(true)
                        .message("Booking cancelled and seats released successfully")
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> searchBookings(
            @AuthenticationPrincipal String userId,
            @RequestParam("query") String query
    ) throws ExecutionException, InterruptedException {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        UserDTO staffUser = userService.getUserById(userId);
        if (staffUser == null || (!"staff".equalsIgnoreCase(staffUser.getRole()) && !"admin".equalsIgnoreCase(staffUser.getRole()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền tìm kiếm vé.");
        }

        List<BookingDTO> results = bookingService.searchBookings(query);

        return ResponseEntity.ok(
                ApiResponse.<List<BookingDTO>>builder()
                        .success(true)
                        .message("Bookings found successfully")
                        .data(results)
                        .build()
        );
    }
}
