package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.*;
import com.cinemabooking.backend.dto.request.SeatBookingRequestDTO;
import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    
    @Autowired
    private Firestore firestore;
    
    @Autowired
    private ShowtimeService showtimeService;
    
    @Autowired
    private MovieService movieService;

    private static final String COLLECTION = "bookings";

    public BookingDTO getBookingById(String bookingId) throws ExecutionException, InterruptedException {
        BookingDTO booking = firestore.collection(BookingDTO.COLLECTION_NAME).document(bookingId).get()
                .get().toObject(BookingDTO.class);

        return booking;
    }

    public BookingDTO createBookingRecord(
            BookingDTO data
    ) throws ExecutionException, InterruptedException{
        WriteBatch batch = firestore.batch();

        batch.set(firestore.collection(COLLECTION).document(data.getBookingId()), data);

        DocumentReference showtimeRef = firestore.collection("showtimes").document(data.getShowtimeId());
        batch.update(showtimeRef, "bookedSeatsCount", FieldValue.increment(data.getSeatIds().size()));

        // Đồng bộ thời gian giữ ghế (heldUntil) với thời gian hết hạn của Booking (createdAt + 7.5 phút)
        long expireTime = data.getCreatedAt() + 450000;
        for (String seatId : data.getSeatIds()) {
            batch.update(firestore.collection("seats").document(seatId),
                    "heldUntil", expireTime
            );
        }

        batch.commit().get();
        return data;
    }

    public BookingDTO getPendingActiveBooking(String userId, String showtimeId) throws ExecutionException, InterruptedException {
        long limitTime = System.currentTimeMillis() - 450000; // 7.5 phút
        List<QueryDocumentSnapshot> docs = firestore.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("showtimeId", showtimeId)
                .whereEqualTo("bookingStatus", "PENDING")
                .get().get().getDocuments();

        for (DocumentSnapshot doc : docs) {
            Long createdAtVal = doc.getLong("createdAt");
            long createdAt = createdAtVal != null ? createdAtVal : 0L;
            if (createdAt > limitTime) {
                BookingDTO booking = doc.toObject(BookingDTO.class);
                if (booking != null) {
                    booking.setBookingId(doc.getId());
                    return booking;
                }
            }
        }
        return null;
    }

    public BookingDTO processBookingCreation(String userId, SeatBookingRequestDTO data) throws ExecutionException, InterruptedException {
        // Kiểm tra xem đã có booking PENDING còn hiệu lực cho user + showtime này chưa
        BookingDTO activePending = getPendingActiveBooking(userId, data.getShowtimeId());
        if (activePending != null) {
            logger.info("[BOOKING_FLOW] User {} already has an active PENDING booking {} for showtime {}. Resuming it...",
                    userId, activePending.getBookingId(), data.getShowtimeId());
            return activePending; // Trả về luôn booking cũ, không tạo mới
        }

        List<SnackOrderSnapshot> orders = new ArrayList<>();
        if (data.getSnackOrders() != null && data.getSnackOrders().size() > 0){
            List<SnackDTO> snacks = firestore.collection("snacks")
                    .whereIn("snackId", data.getSnackOrders().stream().map(SeatBookingRequestDTO.SnackOrder::snackId).collect(Collectors.toList()))
                    .get()
                    .get().toObjects(SnackDTO.class);
            snacks.forEach(snack -> {
                SeatBookingRequestDTO.SnackOrder order = data.getSnackOrders().stream().filter(snackOrder -> snackOrder.snackId().equals(snack.snackId)).findFirst().orElse(null);
                if (order != null) {
                    orders.add(
                            SnackOrderSnapshot.builder()
                                    .snackId(snack.getSnackId())
                                    .snackName(snack.getName())
                                    .snackImgURL(snack.getImageUrl())
                                    .price(snack.getPrice())
                                    .quantity(order.quantity())
                                    .build()
                    );
                }
            });
        }

        String uniqueID = UUID.randomUUID().toString();

        ShowtimeDTO showtime = showtimeService.getShowtimeById(data.getShowtimeId());
        if (showtime == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy suất chiếu.");
        }
        MovieDTO movie = movieService.getMovieById(showtime.getMovieId());

        List<DocumentSnapshot> taskResult = ApiFutures.allAsList(Arrays.asList(
                firestore.collection("rooms").document(showtime.getRoomId()).get(),
                firestore.collection("cinemas").document(showtime.getCinemaId()).get()
        )).get();

        RoomDTO room = taskResult.get(0).toObject(RoomDTO.class);
        CinemaDTO cinema = taskResult.get(1).toObject(CinemaDTO.class);

        List<SeatDTO> seats = firestore.collection(SeatDTO.COLLECTION_NAME)
                .whereIn("seatId", data.getSeatIds())
                .get()
                .get().toObjects(SeatDTO.class);

        // Concurrency and Ownership validation check
        long now = System.currentTimeMillis();
        for (SeatDTO seat : seats) {
            if ("booked".equalsIgnoreCase(seat.getStatus())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ghế " + seat.getSeatCode() + " đã được đặt trước bởi người khác!");
            }
            if (!"held".equalsIgnoreCase(seat.getStatus()) || !userId.equals(seat.getHeldBy()) || seat.getHeldUntil() < now) {
                logger.warn("[SEAT_CONFIRM_FAIL] User {} tried to book seat {} but it is held by user {} until {}",
                        userId, seat.getSeatId(), seat.getHeldBy(), seat.getHeldUntil());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ghế " + seat.getSeatCode() + " chưa được giữ bởi bạn hoặc đã hết hạn giữ ghế!");
            }
        }

        double seatTotal = 0;
        for (SeatDTO seat : seats) {
            double price = "VIP".equalsIgnoreCase(seat.getSeatType()) ? 75000 : 60000;
            seatTotal += price;
        }
        double subTotal = seatTotal + orders.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
        
        // 1. Tính chiết khấu theo hạng thành viên (VIP 10%, Gold 8%, Platinum 15%) tính trên tiền vé
        double discountRank = 0;
        int userPoints = 0;
        try {
            DocumentSnapshot userDoc = firestore.collection("users").document(userId).get().get();
            if (userDoc.exists()) {
                String memberLevel = userDoc.getString("memberLevel");
                double factor = 0.0;
                if (memberLevel != null) {
                    String levelLower = memberLevel.toLowerCase();
                    if (levelLower.contains("vip")) {
                        factor = 0.10;
                    } else if (levelLower.contains("platinum")) {
                        factor = 0.15;
                    } else if (levelLower.contains("gold")) {
                        factor = 0.08;
                    }
                }
                discountRank = seatTotal * factor;

                Long ptsVal = userDoc.getLong("points");
                userPoints = ptsVal != null ? ptsVal.intValue() : 0;
            }
        } catch (Exception e) {
            logger.error("Failed to calculate user member discount", e);
        }

        // 2. Tính chiết khấu theo Voucher / Mã khuyến mãi
        double discountVoucher = 0;
        String promoCode = data.getPromoCode();
        if (promoCode != null && !promoCode.trim().isEmpty()) {
            String code = promoCode.trim().toUpperCase();
            if ("GALAXY50".equals(code)) {
                discountVoucher = 50000;
            } else if ("WELCOME10".equals(code)) {
                discountVoucher = seatTotal * 0.10;
            } else if ("FREESHOP".equals(code)) {
                discountVoucher = 20000;
            }
        }

        // 3. Tính chiết khấu theo điểm Stars và trừ điểm tích lũy
        double discountStars = 0;
        int pointsConsumed = 0;
        if (Boolean.TRUE.equals(data.getUseStars()) && userPoints > 0) {
            discountStars = userPoints * 1000.0;
            pointsConsumed = userPoints;
            try {
                firestore.collection("users").document(userId).update("points", 0).get();
                logger.info("[LOYALTY_STARS] Deducted {} points from user {}", userPoints, userId);
            } catch (Exception e) {
                logger.error("Failed to deduct user points", e);
            }
        }

        double discount = discountRank + discountVoucher + discountStars;
        if (discount > subTotal) {
            discount = subTotal;
        }

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
                .movieTitleSnapshot(movie != null ? movie.getTitle() : "Phim ẩn")
                .movieImageUrlSnapshot(movie != null ? movie.getPosterUrl() : "")
                .roomNameSnapshot(room != null ? room.getName() : "")
                .cinemaNameSnapshot(cinema != null ? cinema.getName() : "")
                .seatIds(data.getSeatIds())
                .seatCodes(seats.stream().map(SeatDTO::getSeatCode).collect(Collectors.toList()))
                .snackOrder(orders)
                .subtotal(subTotal)
                .discount(discount)
                .total(subTotal - discount)
                .pointsConsumed(pointsConsumed)
                .paymentMethod(data.getPaymentMethod().name())
                .paymentStatus("PENDING")
                .paymentCode(paymentCode)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        logger.info("[BOOKING_FLOW] Creating booking record: bookingId={}, userId={}, total={}, status=PENDING",
                uniqueID, userId, subTotal - discount);

        return createBookingRecord(booking);
    }

    public void updatePaymentStatus(String bookingId, String paymentStatus, String bookingStatus) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(bookingId).set(
                        BookingDTO.builder()
                                .paymentStatus(paymentStatus)
                                .bookingStatus(bookingStatus)
                                .paymentAt("SUCCESS".equalsIgnoreCase(paymentStatus) || "confirmed".equalsIgnoreCase(paymentStatus) ? System.currentTimeMillis() : 0)
                                .updatedAt(System.currentTimeMillis())
                                .build(),
                        SetOptions.mergeFields("paymentStatus", "bookingStatus", "paymentAt", "updatedAt"))
                .get();
    }

    public void confirmBookingAndSeats(String bookingId) throws ExecutionException, InterruptedException {
        DocumentReference bookingRef = firestore.collection(COLLECTION).document(bookingId);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot bookingSnap = transaction.get(bookingRef).get();
            if (!bookingSnap.exists()) {
                throw new RuntimeException("Booking not found");
            }
            BookingDTO booking = bookingSnap.toObject(BookingDTO.class);
            if (booking == null) {
                throw new RuntimeException("Booking data is invalid");
            }

            // Check if seats are already booked/held by others
            List<String> seatIds = booking.getSeatIds();
            if (seatIds != null && !seatIds.isEmpty()) {
                long now = System.currentTimeMillis();
                List<DocumentReference> refs = new ArrayList<>();
                for (String seatId : seatIds) {
                    refs.add(firestore.collection("seats").document(seatId));
                }

                List<DocumentSnapshot> snapshots = transaction.getAll(refs.toArray(new DocumentReference[0])).get();
                for (DocumentSnapshot doc : snapshots) {
                    if (!doc.exists()) {
                        throw new RuntimeException("Ghế " + doc.getId() + " không tồn tại");
                    }

                    String status = doc.getString("status");
                    String heldBy = doc.getString("heldBy");
                    Long heldUntilVal = doc.getLong("heldUntil");
                    long heldUntil = heldUntilVal != null ? heldUntilVal : 0L;
                    String bookedBy = doc.getString("bookedBy");

                    if ("booked".equalsIgnoreCase(status)) {
                        if (booking.getUserId().equals(bookedBy)) {
                            continue;
                        }
                        throw new RuntimeException("Ghế " + doc.getId() + " đã được đặt bởi người khác");
                    }

                    boolean isHeldBySelf = "held".equalsIgnoreCase(status) && booking.getUserId().equals(heldBy);
                    boolean isAvailable = "available".equalsIgnoreCase(status) || status == null || status.isEmpty();

                    if (!isHeldBySelf && !isAvailable) {
                        throw new RuntimeException("Ghế " + doc.getId() + " không khả dụng hoặc đã được giữ/đặt bởi người khác!");
                    }
                }

                // Update seats to booked
                for (DocumentReference ref : refs) {
                    logger.info("[PAYMENT_SUCCESS] Booking {} confirmed. Marking seat {} as booked by {}", bookingId, ref.getId(), booking.getUserId());
                    transaction.update(ref,
                            "status", "booked",
                            "bookedBy", booking.getUserId(),
                            "bookedAt", now,
                            "heldBy", null,
                            "heldUntil", 0L
                    );
                }
            }

            // Update booking status
            transaction.update(bookingRef,
                    "paymentStatus", "SUCCESS",
                    "bookingStatus", "CONFIRMED",
                    "paymentAt", System.currentTimeMillis(),
                    "updatedAt", System.currentTimeMillis()
            );

            return null;
        }).get();
    }

    public void cancelBookingAndReleaseSeats(String bookingId) throws ExecutionException, InterruptedException {
        DocumentReference bookingRef = firestore.collection(COLLECTION).document(bookingId);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot bookingSnap = transaction.get(bookingRef).get();
            if (!bookingSnap.exists()) {
                throw new RuntimeException("Booking not found");
            }
            BookingDTO booking = bookingSnap.toObject(BookingDTO.class);
            if (booking == null) {
                throw new RuntimeException("Booking data is invalid");
            }

            // Read all seats first, before doing any write operations
            List<String> seatIds = booking.getSeatIds();
            List<DocumentSnapshot> seatSnaps = new ArrayList<>();
            List<DocumentReference> seatRefs = new ArrayList<>();
            if (seatIds != null && !seatIds.isEmpty()) {
                for (String seatId : seatIds) {
                    seatRefs.add(firestore.collection("seats").document(seatId));
                }
                seatSnaps = transaction.getAll(seatRefs.toArray(new DocumentReference[0])).get();
            }

            // Now perform write operations
            // Hoàn trả điểm Stars nếu có
            int pointsRefund = booking.getPointsConsumed();
            if (pointsRefund > 0) {
                DocumentReference userRef = firestore.collection("users").document(booking.getUserId());
                transaction.update(userRef, "points", FieldValue.increment(pointsRefund));
                logger.info("[LOYALTY_REFUND] Refunded {} points to user {}", pointsRefund, booking.getUserId());
            }

            if (seatIds != null && !seatIds.isEmpty()) {
                for (int i = 0; i < seatRefs.size(); i++) {
                    DocumentReference seatRef = seatRefs.get(i);
                    DocumentSnapshot seatSnap = seatSnaps.get(i);
                    if (seatSnap.exists()) {
                        String status = seatSnap.getString("status");
                        String heldBy = seatSnap.getString("heldBy");
                        String bookedBy = seatSnap.getString("bookedBy");

                        boolean isHeldBySelf = "held".equalsIgnoreCase(status) && booking.getUserId().equals(heldBy);
                        boolean isBookedBySelf = "booked".equalsIgnoreCase(status) && booking.getUserId().equals(bookedBy);

                        if (isHeldBySelf || isBookedBySelf) {
                            logger.info("[RELEASE_SEAT] Booking {} failed/cancelled. Releasing seat {} (previously {})", bookingId, seatRef.getId(), status);
                            transaction.update(seatRef,
                                    "status", "available",
                                    "heldBy", null,
                                    "heldUntil", 0L,
                                    "bookedBy", null,
                                    "bookedAt", null
                            );
                        }
                    }
                }
            }

            // Decrease booked seats count of showtime
            if (booking.getShowtimeId() != null && seatIds != null && !seatIds.isEmpty()) {
                DocumentReference showtimeRef = firestore.collection("showtimes").document(booking.getShowtimeId());
                transaction.update(showtimeRef, "bookedSeatsCount", FieldValue.increment(-seatIds.size()));
            }

            // Update booking status to CANCELLED and payment to FAILED
            transaction.update(bookingRef,
                    "paymentStatus", "FAILED",
                    "bookingStatus", "CANCELLED",
                    "updatedAt", System.currentTimeMillis()
            );

            return null;
        }).get();
    }

    public void confirmBookingSeats(String bookingId) throws ExecutionException, InterruptedException {
        BookingDTO booking = getBookingById(bookingId);
        if (booking == null) {
            logger.error("[PAYMENT_FAILED] Booking not found for ID: {}", bookingId);
            throw new RuntimeException("Booking not found");
        }

        List<String> seatIds = booking.getSeatIds();
        if (seatIds == null || seatIds.isEmpty()) {
            logger.warn("Booking {} has no seat IDs associated", bookingId);
            return;
        }

        // Dùng Transaction để tránh Double Booking
        firestore.runTransaction(transaction -> {
            long now = System.currentTimeMillis();
            List<DocumentReference> refs = new ArrayList<>();
            for (String seatId : seatIds) {
                refs.add(firestore.collection("seats").document(seatId));
            }

            // Đọc toàn bộ các ghế trong transaction
            List<DocumentSnapshot> snapshots = transaction.getAll(refs.toArray(new DocumentReference[0])).get();
            for (DocumentSnapshot doc : snapshots) {
                if (!doc.exists()) {
                    throw new RuntimeException("Ghế " + doc.getId() + " không tồn tại");
                }

                String status = doc.getString("status");
                String heldBy = doc.getString("heldBy");
                Long heldUntilVal = doc.getLong("heldUntil");
                long heldUntil = heldUntilVal != null ? heldUntilVal : 0L;
                String bookedBy = doc.getString("bookedBy");

                if ("booked".equalsIgnoreCase(status)) {
                    if (booking.getUserId().equals(bookedBy)) {
                        // Đã được đặt bởi chính user này (gọi confirm nhiều lần), bỏ qua
                        continue;
                    }
                    throw new RuntimeException("Ghế " + doc.getId() + " đã được đặt bởi người khác");
                }

                // Phải đang giữ bởi đúng user này và chưa hết hạn
                if (!"held".equalsIgnoreCase(status) || !booking.getUserId().equals(heldBy) || heldUntil < now) {
                    throw new RuntimeException("Ghế " + doc.getId() + " chưa được giữ bởi bạn hoặc giữ ghế đã hết hạn!");
                }
            }

            // Ghi cập nhật ghế
            for (DocumentReference ref : refs) {
                logger.info("[PAYMENT_SUCCESS] Booking {} confirmed. Marking seat {} as booked by {}", bookingId, ref.getId(), booking.getUserId());
                transaction.update(ref,
                        "status", "booked",
                        "bookedBy", booking.getUserId(),
                        "bookedAt", now
                );
            }
            return null;
        }).get();
    }

    public void releaseBookingSeats(String bookingId) throws ExecutionException, InterruptedException {
        DocumentReference bookingRef = firestore.collection(COLLECTION).document(bookingId);

        firestore.runTransaction(transaction -> {
            DocumentSnapshot bookingSnap = transaction.get(bookingRef).get();
            if (!bookingSnap.exists()) return null;
            BookingDTO booking = bookingSnap.toObject(BookingDTO.class);
            if (booking == null) return null;

            List<String> seatIds = booking.getSeatIds();
            if (seatIds == null || seatIds.isEmpty()) return null;

            // Read all seats first, before doing any write operations
            List<DocumentSnapshot> seatSnaps = new ArrayList<>();
            List<DocumentReference> seatRefs = new ArrayList<>();
            for (String seatId : seatIds) {
                seatRefs.add(firestore.collection("seats").document(seatId));
            }
            seatSnaps = transaction.getAll(seatRefs.toArray(new DocumentReference[0])).get();

            for (int i = 0; i < seatRefs.size(); i++) {
                DocumentReference seatRef = seatRefs.get(i);
                DocumentSnapshot seatSnap = seatSnaps.get(i);
                if (seatSnap.exists()) {
                    String status = seatSnap.getString("status");
                    String heldBy = seatSnap.getString("heldBy");
                    String bookedBy = seatSnap.getString("bookedBy");

                    boolean isHeldBySelf = "held".equalsIgnoreCase(status) && booking.getUserId().equals(heldBy);
                    boolean isBookedBySelf = "booked".equalsIgnoreCase(status) && booking.getUserId().equals(bookedBy);

                    if (isHeldBySelf || isBookedBySelf) {
                        logger.info("[RELEASE_SEAT] Booking {} failed/cancelled. Releasing seat {} (previously {})", bookingId, seatRef.getId(), status);
                        transaction.update(seatRef,
                                "status", "available",
                                "heldBy", null,
                                "heldUntil", 0L,
                                "bookedBy", null,
                                "bookedAt", null
                        );
                    }
                }
            }

            DocumentReference showtimeRef = firestore.collection("showtimes").document(booking.getShowtimeId());
            transaction.update(showtimeRef, "bookedSeatsCount", FieldValue.increment(-seatIds.size()));
            return null;
        }).get();
    }

    public List<BookingDTO> searchBookings(String query) throws ExecutionException, InterruptedException {
        List<String> userIds = new ArrayList<>();
        List<QueryDocumentSnapshot> users = firestore.collection("users")
                .get().get().getDocuments();
        for (QueryDocumentSnapshot doc : users) {
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
        List<QueryDocumentSnapshot> bookings = firestore.collection("bookings")
                .get().get().getDocuments();
        for (QueryDocumentSnapshot doc : bookings) {
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
                        DocumentSnapshot userDoc = firestore.collection("users").document(booking.getUserId()).get().get();
                        if (userDoc.exists()) {
                            UserDTO user = UserDTO.builder()
                                    .uid(userDoc.getId())
                                    .email(userDoc.getString("email"))
                                    .phone(userDoc.getString("phone"))
                                    .name(userDoc.getString("name"))
                                    .role(userDoc.getString("role"))
                                    .status(userDoc.getString("status"))
                                    .build();
                            booking.setUser(user);
                        }
                    } catch (Exception ignored) {}
                    results.add(booking);
                }
            }
        }
        return results;
    }

    public void updateCheckInTime(String bookingId, long checkInAt) throws ExecutionException, InterruptedException {
        firestore.collection(COLLECTION).document(bookingId).set(
                        BookingDTO.builder()
                                .checkInAt(checkInAt)
                                .updatedAt(System.currentTimeMillis())
                                .build(),
                        com.google.cloud.firestore.SetOptions.mergeFields("checkInAt", "updatedAt"))
                .get();
    }
}
