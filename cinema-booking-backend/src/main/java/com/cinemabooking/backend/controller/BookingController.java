package com.cinemabooking.backend.controller;

import com.cinemabooking.backend.dto.*;
import com.cinemabooking.backend.dto.request.SeatBookingRequestDTO;
import com.cinemabooking.backend.service.BookingService;
import com.cinemabooking.backend.service.MovieService;
import com.cinemabooking.backend.service.ShowtimeService;
import com.cinemabooking.backend.service.UserService;
import com.cinemabooking.backend.payment.service.PaymentService;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.DocumentSnapshot;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

    @PostMapping
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(
            @AuthenticationPrincipal String userId,
            @RequestBody SeatBookingRequestDTO bookingRequest
    ) throws ExecutionException, InterruptedException {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập.");
        }
        SeatBookingRequestDTO data = bookingRequest;
        List<SnackOrderSnapshot> orders = new ArrayList<>();
        if (data.getSnackOrders() != null && data.getSnackOrders().size() > 0){
            List<SnackDTO> snacks = firestore.collection("snacks")
                    .whereIn("snackId", data.getSnackOrders().stream().map(item -> item.snackId()).collect(Collectors.toList()) )
                    .get()
                    .get().toObjects(SnackDTO.class);
            snacks.stream().forEach(snack -> {
                SeatBookingRequestDTO.SnackOrder order = data.getSnackOrders().stream().filter(snackOrder -> snackOrder.snackId().equals(snack.snackId)).findFirst().orElse(null);
                orders.add(
                        SnackOrderSnapshot.builder()
                                .snackId(snack.getSnackId())
                                .snackName(snack.getName())
                                .snackImgURL(snack.getImageUrl())
                                .price(snack.getPrice())
                                .quantity(order.quantity())
                                .build()
                );
            });
        }

        String uniqueID = UUID.randomUUID().toString();

        ShowtimeDTO showtime = showtimeService.getShowtimeById(data.getShowtimeId());
        MovieDTO movie = movieService.getMovieById(showtime.getMovieId());

        List<DocumentSnapshot> taskResult = ApiFutures.allAsList(Arrays.asList(
                firestore.collection("rooms").document(showtime.getRoomId()).get(),
                firestore.collection("cinemas").document(showtime.getCinemaId()).get()
        )).get();

        RoomDTO room = taskResult.get(0).toObject(RoomDTO.class);
        CinemaDTO cinema = taskResult.get(1).toObject(CinemaDTO.class);

        List<SeatDTO> seats = firestore.collection(SeatDTO.COLLECTION_NAME)
                .whereIn("seatId", data.getSeatIds())
                // kiem tra ghe co thuoc phong chieu khong
                .get()
                .get().toObjects(SeatDTO.class);

        // Concurrency and Ownership validation check
        long now = System.currentTimeMillis();
        for (SeatDTO seat : seats) {
            if ("booked".equalsIgnoreCase(seat.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ghế " + seat.getSeatCode() + " đã được đặt trước bởi người khác!");
            }
            if (!"held".equalsIgnoreCase(seat.getStatus()) || !userId.equals(seat.getHeldBy()) || seat.getHeldUntil() < now) {
                log.warn("[SEAT_CONFIRM_FAIL] User {} tried to book seat {} but it is held by user {} until {}",
                        userId, seat.getSeatId(), seat.getHeldBy(), seat.getHeldUntil());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ghế " + seat.getSeatCode() + " chưa được giữ bởi bạn hoặc đã hết hạn giữ ghế!");
            }
        }

        double subTotal = showtime.getBasePrice() * seats.size() + orders.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        double discount = 0;

        String suffix = uniqueID.contains("_") ? uniqueID.substring(uniqueID.indexOf("_") + 1) : uniqueID;
        if (suffix.length() > 8) {
            suffix = suffix.substring(0, 8);
        }
        String paymentCode = ("BK" + suffix).toUpperCase();

        BookingDTO booking = BookingDTO.builder()
                .bookingId(uniqueID)
                .bookingStatus("PENDING")
                .userId(userId)
                .movieId(showtime.getMovieId())
                .showtimeId(data.getShowtimeId())
                .showtimeStartAtSnapshot(showtime.getStartAt())
                .movieTitleSnapshot(movie.getTitle())
                .movieImageUrlSnapshot(movie.getPosterUrl())
                .roomNameSnapshot(room.getName())
                .cinemaNameSnapshot(cinema.getName())
                .seatIds(data.getSeatIds())
                .seatCodes(seats.stream().map(seat -> seat.getSeatCode()).collect(Collectors.toList()))
                .snackOrder(orders)
                .subtotal(subTotal)
                .discount(discount)
                .total(subTotal - discount)
                // cap nhat payment
                .paymentMethod(bookingRequest.getPaymentMethod().name())
                .paymentStatus("PENDING")
                .paymentCode(paymentCode)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        log.info("[BOOKING_FLOW] Creating booking record: bookingId={}, userId={}, total={}, status=PENDING",
                uniqueID, userId, subTotal - discount);

        BookingDTO bookingDTO = bookingService.createBooking(booking);

        // Tạo document payment với status = PENDING
        paymentService.createPendingPayment(
                bookingDTO.getBookingId(),
                bookingDTO.getUserId(),
                bookingDTO.getPaymentMethod(),
                bookingDTO.getTotal()
        );

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
//        if (!userId.equals(booking.getUserId())) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xác nhận vé này.");
//        }
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

        List<String> userIds = new ArrayList<>();
        List<com.google.cloud.firestore.QueryDocumentSnapshot> users = firestore.collection("users")
                .get().get().getDocuments();
        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : users) {
            String name = doc.getString("name");
            String email = doc.getString("email");
            String phone = doc.getString("phone");
            if ((name != null && name.toLowerCase().contains(query.toLowerCase())) ||
                    (email != null && email.toLowerCase().contains(query.toLowerCase())) ||
                    (phone != null && phone.contains(query))) {
                userIds.add(doc.getId());
            }
        }

        List<BookingDTO> results = new ArrayList<>();
        List<com.google.cloud.firestore.QueryDocumentSnapshot> bookings = firestore.collection("bookings")
                .get().get().getDocuments();
        for (com.google.cloud.firestore.QueryDocumentSnapshot doc : bookings) {
            BookingDTO booking = doc.toObject(BookingDTO.class);
            if (booking != null) {
                booking.setBookingId(doc.getId());
                boolean matchesUser = userIds.contains(booking.getUserId());
                boolean matchesBookingId = booking.getBookingId().equalsIgnoreCase(query) || booking.getBookingId().toLowerCase().contains(query.toLowerCase());
                boolean matchesPaymentCode = booking.getPaymentCode() != null && booking.getPaymentCode().equalsIgnoreCase(query);
                if (matchesUser || matchesBookingId || matchesPaymentCode) {
                    try {
                        ShowtimeDTO showTime = showtimeService.getShowtimeById(booking.getShowtimeId());
                        booking.setShowtime(showTime);
                    } catch (Exception ignored) {}
                    try {
                        UserDTO user = userService.getUserById(booking.getUserId());
                        booking.setUser(user);
                    } catch (Exception ignored) {}
                    results.add(booking);
                }
            }
        }

        return ResponseEntity.ok(
                ApiResponse.<List<BookingDTO>>builder()
                        .success(true)
                        .message("Bookings found successfully")
                        .data(results)
                        .build()
        );
    }
}
