package com.cinemabooking.backend.service;

import com.cinemabooking.backend.dto.ChatMessage;
import com.cinemabooking.backend.dto.Conversation;
import com.cinemabooking.backend.dto.request.SendMessageRequest;
import com.cinemabooking.backend.dto.MovieDTO;
import com.cinemabooking.backend.dto.ShowtimeDTO;
import com.cinemabooking.backend.dto.CinemaDTO;
import com.cinemabooking.backend.dto.BookingDTO;
import com.google.cloud.firestore.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class ChatService {

    @Autowired private MovieService movieService;
    @Autowired private ShowtimeService showtimeService;
    @Autowired private CinemaService cinemaService;
    @Autowired private BookingService bookingService;

    @Autowired private Firestore firestore;
    @Autowired private ConversationService conversationService;

    // Send a message
    public ChatMessage sendMessage(String senderId, SendMessageRequest req)
            throws ExecutionException, InterruptedException {

        String receiverId = req.getReceiverId();
        long now = System.currentTimeMillis();
        Conversation convo = null;

        if ("SUPPORT_BOT".equals(receiverId)) {
            convo = conversationService.getConversationByUserIds(senderId, "SUPPORT_BOT");
            if (convo == null) {
                convo = conversationService.createSupportConversation(senderId, now);
            } else if ("RESOLVED".equals(convo.getStatus()) || "CLOSED".equals(convo.getStatus())) {
                convo.setStatus("BOT_ONLY");
                convo.setAssignedStaffId(null);
                List<String> pIds = new ArrayList<>();
                pIds.add(senderId);
                pIds.add("SUPPORT_BOT");
                convo.setParticipantIds(pIds);
                
                List<Conversation.UserSnapShot> pSnaps = convo.getParticipants().stream()
                        .filter(p -> p.getUserId().equals(senderId) || p.getUserId().equals("SUPPORT_BOT"))
                        .collect(Collectors.toList());
                convo.setParticipants(pSnaps);
                
                firestore.collection(Conversation.COLLECTION_NAME).document(convo.getConvoId()).set(convo).get();
            }
        } else {
            convo = conversationService.getConversationByUserIds(senderId, receiverId);
            if (convo == null) {
                convo = conversationService.createNewConversation(Arrays.asList(senderId, receiverId), now);
            }
        }

        ChatMessage message = ChatMessage.builder()
                .convoId(convo.getConvoId())
                .senderId(senderId)
                .receiverId(receiverId)
                .content(req.getContent())
                .imgUrl(req.getImgUrl())
                .sentAt(now)
                .build();

        message = saveMessage(message);

        conversationService.updateConversationAfterMessage(
                convo.getConvoId(), message, receiverId, now);

        log.info("Message {} sent from {} to {} in convo {}", message.getMessageId(), senderId, receiverId, convo.getConvoId());

        if ("SUPPORT_BOT".equals(receiverId)) {
            if (!"BOT_ONLY".equals(convo.getStatus())) {
                convo.setStatus("BOT_ONLY");
                convo.setAssignedStaffId(null);
                firestore.collection(Conversation.COLLECTION_NAME).document(convo.getConvoId())
                        .update("status", "BOT_ONLY", "assignedStaffId", null).get();
            }
            handleBotResponse(convo, message);
        }

        return message;
    }

    // Fetch messages (paginated)
    public List<ChatMessage> getMessages(String convoId, int limit, Long beforeTimestamp) throws ExecutionException, InterruptedException {
        List<ChatMessage> messages = firestore.collection(ChatMessage.COLLECTION_NAME)
                .whereEqualTo("convoId", convoId)
                .orderBy("sentAt", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .get().toObjects(ChatMessage.class);
        return messages;
    }

    // Helpers
    private ChatMessage saveMessage(ChatMessage message) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(ChatMessage.COLLECTION_NAME).document();
        message.setMessageId(docRef.getId());
        docRef.set(message).get();
        return message;
    }

    private boolean matchesAny(String content, String[] keywords) {
        for (String kw : keywords) {
            if (content.contains(kw)) return true;
        }
        return false;
    }

    public void handleBotResponse(Conversation convo, ChatMessage customerMsg) throws ExecutionException, InterruptedException {
        if (customerMsg == null || customerMsg.getContent() == null) {
            return;
        }
        String content = customerMsg.getContent().trim().toLowerCase();
        String customerId = customerMsg.getSenderId();
        long now = System.currentTimeMillis();
        
        String replyContent = "";
        boolean matchedIntent = false;

        // 1. Phim đang chiếu
        String[] nowShowingKws = {"phim đang chiếu", "phim dang chieu", "đang chiếu", "dang chieu", "phim hot", "phim moi", "phim mới", "phim chieu", "phim chiếu", "danh sách phim", "danh sach phim"};
        
        // 2. Suất chiếu hôm nay & Lịch chiếu
        String[] todayShowtimeKws = {"suất chiếu hôm nay", "suat chieu hom nay", "suất hôm nay", "suat hom nay", "lịch chiếu hôm nay", "lich chieu hom nay", "suất chiếu trong ngày", "suat chieu trong ngay"};
        String[] showtimeKws = {"lịch chiếu", "lich chieu", "lịch phim", "lich phim", "xem lịch", "xem lich", "giờ chiếu", "gio chieu", "suất chiếu", "suat chieu"};

        // 3. Rạp chiếu & giờ mở cửa
        String[] cinemaKws = {"rạp chiếu", "rap chieu", "rạp", "rap", "chi nhánh", "chi nhanh", "địa chỉ", "dia chi", "ở đâu", "o dau", "dia diem", "địa điểm", "giờ mở cửa", "gio mo cua"};

        // 4. Giá vé
        String[] priceKws = {"giá vé", "gia ve", "vé bao nhiêu", "ve bao nhieu", "một vé bao nhiêu", "mot ve bao nhieu", "nhiêu tiền", "nhieu tien", "tiền vé", "tien ve", "giá cả", "gia ca", "bao nhiêu tiền", "bao nhieu tien", "loại vé", "loai ve", "vé vip", "ve vip", "vé thường", "ve thuong"};

        // 5. Voucher & Khuyến mãi
        String[] voucherKws = {"voucher của tôi", "voucher cua toi", "voucher", "mã giảm giá", "ma giam gia", "khuyến mãi", "khuyen mai", "giảm giá", "giam gia", "uu dai", "ưu đãi", "mã khuyến mãi", "ma khuyen mai", "mã ưu đãi", "ma uu dai"};

        // 6. Đặt vé & Thanh toán
        String[] paymentKws = {"thanh toán", "thanh toan", "cổng thanh toán", "cong thanh toan", "atm", "visa", "chuyển khoản", "chuyen khoan", "pay", "cách mua vé", "cach mua ve", "đặt vé", "dat ve"};

        // 7. MoMo / lỗi thanh toán
        String[] payIssueKws = {"momo", "zalopay", "lỗi thanh toán", "loi thanh toan", "thanh toán lỗi", "thanh toan loi", "không thanh toán được", "khong thanh toan duoc", "lỗi chuyển tiền", "loi chuyen tien", "bị trừ tiền", "bi tru tien", "lỗi trừ tiền", "loi tru tien", "trừ tiền không có vé", "tru tien khong co ve", "lỗi nạp", "loi nap"};

        // 8. Lịch sử vé & mã QR
        String[] myBookingKws = {"vé của tôi", "ve cua toi", "booking của tôi", "booking cua toi", "lịch sử đặt vé", "lich su dat ve", "vé đã đặt", "ve da dat", "my booking", "my ticket", "vé đặt", "ve dat", "lịch sử mua", "lich su mua", "vé của t", "ve cua t", "booking cua t", "booking của t", "mã qr", "ma qr", "qr"};

        // 9. Hủy vé & hoàn tiền
        String[] refundKws = {"hoàn tiền", "hoan tien", "hoàn vé", "hoan ve", "hủy vé", "huy ve", "trả vé", "tra ve", "hoàn lại", "hoan lai", "refund"};

        // 10. Tài khoản & Đăng nhập
        String[] accountKws = {"tài khoản", "tai khoan", "đăng nhập", "dang nhap", "profile", "thông tin cá nhân", "thong tin ca nhan", "mật khẩu", "mat khau", "đăng ký", "dang ky"};

        if (matchesAny(content, nowShowingKws)) {
            try {
                log.info("Chatbot Movie Query - status: NOW_SHOWING, page: 0, size: 50");
                List<MovieDTO> nowShowing = movieService.getMoviesByStatus("NOW_SHOWING", 0, 50);
                log.info("Chatbot Movie Result - count: {}, status filtered: NOW_SHOWING", nowShowing.size());
                if (nowShowing.isEmpty()) {
                    replyContent = "Hiện tại hệ thống không có phim nào đang chiếu. Bạn vui lòng quay lại sau nhé!";
                } else {
                    StringBuilder sb = new StringBuilder("Phim đang chiếu:\n");
                    int limit = Math.min(nowShowing.size(), 6);
                    for (int i = 0; i < limit; i++) {
                        sb.append("- ").append(nowShowing.get(i).getTitle()).append("\n");
                    }
                    if (nowShowing.size() > limit) {
                        sb.append("... và nhiều phim khác.\n");
                    }
                    sb.append("Gõ \"suất chiếu hôm nay\" để xem lịch chiếu.");
                    replyContent = sb.toString();
                }
            } catch (Exception e) {
                log.error("Error fetching movies: ", e);
                replyContent = "Không thể tải danh sách phim đang chiếu lúc này. Vui lòng quay lại sau!";
            }
            matchedIntent = true;
        } else if (matchesAny(content, todayShowtimeKws)) {
            try {
                java.util.Calendar cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT+7"));
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                long todayStart = cal.getTimeInMillis();
                long todayEnd = todayStart + 24 * 60 * 60 * 1000L - 1;

                List<ShowtimeDTO> showtimes = showtimeService.getAllShowtimes().stream()
                        .filter(s -> s.getStartAt() >= todayStart && s.getStartAt() <= todayEnd)
                        .collect(Collectors.toList());

                if (showtimes.isEmpty()) {
                    replyContent = "Hôm nay hiện tại không có suất chiếu nào khả dụng. Bạn vui lòng quay lại sau nhé!";
                } else {
                    List<MovieDTO> allMovies = movieService.getAllMovies(0, 1000);
                    Map<String, String> movieTitleMap = allMovies.stream()
                            .collect(Collectors.toMap(MovieDTO::getMovieId, MovieDTO::getTitle, (a, b) -> a));
                    List<CinemaDTO> allCinemas = cinemaService.getAllCinemas();
                    Map<String, String> cinemaNameMap = allCinemas.stream()
                            .collect(Collectors.toMap(CinemaDTO::getCinemaId, CinemaDTO::getName, (a, b) -> a));

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm");
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));

                    // Group by Movie - Cinema
                    Map<String, List<String>> grouped = new HashMap<>();
                    for (ShowtimeDTO s : showtimes) {
                        String movieTitle = movieTitleMap.getOrDefault(s.getMovieId(), "Phim ẩn");
                        String cinemaName = cinemaNameMap.getOrDefault(s.getCinemaId(), "Rạp ẩn");
                        String timeStr = sdf.format(new Date(s.getStartAt()));
                        String key = movieTitle + " (" + cinemaName + ")";
                        grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(timeStr);
                    }

                    StringBuilder sb = new StringBuilder("Lịch chiếu hôm nay:\n");
                    int count = 0;
                    for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
                        if (count >= 5) {
                            sb.append("... và một số suất chiếu khác. Xem chi tiết trong app.");
                            break;
                        }
                        java.util.Collections.sort(entry.getValue());
                        sb.append("- ").append(entry.getKey()).append(": ")
                          .append(String.join(", ", entry.getValue())).append("\n");
                        count++;
                    }
                    replyContent = sb.toString().trim();
                }
            } catch (Exception e) {
                log.error("Error fetching showtimes: ", e);
                replyContent = "Không thể lấy danh sách suất chiếu lúc này. Bạn vui lòng quay lại sau!";
            }
            matchedIntent = true;
        } else if (matchesAny(content, showtimeKws)) {
            replyContent = "Để xem lịch chiếu chi tiết, bạn vui lòng chọn mục 'Rạp Phim' hoặc 'Điện Ảnh' ở thanh điều hướng bên dưới, chọn phim và nhấn 'Đặt vé' để xem các suất chiếu khả dụng. Hoặc bạn có thể hỏi tôi 'suất chiếu hôm nay' để xem các suất chiếu trong ngày.";
            matchedIntent = true;
        } else if (matchesAny(content, cinemaKws)) {
            try {
                List<CinemaDTO> cinemas = cinemaService.getAllCinemas();
                if (cinemas.isEmpty()) {
                    replyContent = "Hệ thống CinemaBookingApp hiện chưa cấu hình rạp chiếu (mở cửa từ 8:00 - 23:30 hằng ngày). Vui lòng quay lại sau!";
                } else {
                    StringBuilder sb = new StringBuilder("Danh sách rạp chiếu (mở cửa 8h00 - 23h30):\n");
                    for (CinemaDTO c : cinemas) {
                        sb.append("- ").append(c.getName()).append(": ").append(c.getAddress()).append("\n");
                    }
                    replyContent = sb.toString().trim();
                }
            } catch (Exception e) {
                log.error("Error fetching cinemas: ", e);
                replyContent = "Không thể tải thông tin rạp chiếu lúc này.";
            }
            matchedIntent = true;
        } else if (matchesAny(content, priceKws)) {
            replyContent = "Giá vé dao động từ 70.000đ - 120.000đ tùy theo loại ghế (Thường/VIP), định dạng phim (2D/3D) và khung giờ chiếu (Ngày thường/Cuối tuần/Ngày lễ).\nBạn có thể xem giá chi tiết của từng suất chiếu khi tiến hành đặt vé.";
            matchedIntent = true;
        } else if (matchesAny(content, voucherKws)) {
            try {
                List<QueryDocumentSnapshot> voucherDocs = firestore.collection("vouchers")
                        .whereEqualTo("userId", customerId)
                        .get().get().getDocuments();
                List<Map<String, Object>> userVouchers = new ArrayList<>();
                for (DocumentSnapshot doc : voucherDocs) {
                    Map<String, Object> data = doc.getData();
                    if (data != null) {
                        data.put("voucherId", doc.getId());
                        userVouchers.add(data);
                    }
                }
                userVouchers.sort((v1, v2) -> {
                    long t1 = v1.get("createdAt") != null ? ((Number) v1.get("createdAt")).longValue() : 0L;
                    long t2 = v2.get("createdAt") != null ? ((Number) v2.get("createdAt")).longValue() : 0L;
                    return Long.compare(t2, t1);
                });

                if (userVouchers.isEmpty()) {
                    replyContent = "Bạn chưa có mã giảm giá hoặc voucher nào trong tài khoản.";
                } else {
                    StringBuilder sb = new StringBuilder("Voucher của bạn:\n");
                    int limit = Math.min(userVouchers.size(), 3);
                    for (int i = 0; i < limit; i++) {
                        Map<String, Object> v = userVouchers.get(i);
                        String vId = (String) v.get("voucherId");
                        Number val = (Number) v.get("discountValue");
                        double discount = val != null ? val.doubleValue() : 0.0;
                        Boolean used = (Boolean) v.get("isUsed");
                        boolean isUsed = used != null && used;
                        String status = isUsed ? "Đã dùng" : "Chưa dùng";
                        
                        sb.append("- ").append(vId)
                          .append(" | Giảm: ").append(String.format("%,.0fđ", discount))
                          .append(" (").append(status).append(")\n");
                    }
                    if (userVouchers.size() > limit) {
                        sb.append("... và một số voucher khác.");
                    }
                    replyContent = sb.toString().trim();
                }
            } catch (Exception e) {
                log.error("Error fetching vouchers: ", e);
                replyContent = "Không thể lấy thông tin voucher của bạn lúc này.";
            }
            matchedIntent = true;
        } else if (matchesAny(content, paymentKws)) {
            replyContent = "Hệ thống hỗ trợ thanh toán qua nhiều cổng tiện lợi:\n- Ví điện tử: MoMo, ZaloPay\n- Thẻ nội địa (ATM/Napas)\n- Thẻ quốc tế (Visa/Mastercard)\nKhi đặt vé, bạn chỉ cần chọn phương thức thanh toán phù hợp và làm theo hướng dẫn.";
            matchedIntent = true;
        } else if (matchesAny(content, payIssueKws)) {
            replyContent = "Hệ thống ghi nhận bạn gặp sự cố thanh toán (MoMo/ZaloPay/Thẻ). Bạn vui lòng liên hệ Hotline 1900 xxxx hoặc mang biên lai giao dịch/lịch sử trừ tiền đến trực tiếp quầy vé tại rạp để được nhân viên kiểm tra và xuất vé hỗ trợ nhanh nhất nhé!";
            matchedIntent = true;
        } else if (matchesAny(content, myBookingKws)) {
            try {
                List<QueryDocumentSnapshot> bookingDocs = firestore.collection("bookings")
                        .whereEqualTo("userId", customerId)
                        .get().get().getDocuments();
                List<BookingDTO> userBookings = bookingDocs.stream()
                        .map(doc -> doc.toObject(BookingDTO.class))
                        .filter(b -> b != null && !b.isDeleted())
                        .sorted((b1, b2) -> Long.compare(b2.getCreatedAt(), b1.getCreatedAt()))
                        .limit(3)
                        .collect(Collectors.toList());

                if (userBookings.isEmpty()) {
                    replyContent = "Bạn chưa có giao dịch đặt vé nào trên hệ thống.";
                } else {
                    StringBuilder sb = new StringBuilder("Vé đặt gần đây của bạn:\n");
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                    sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+7"));
                    for (BookingDTO b : userBookings) {
                        String timeStr = sdf.format(new Date(b.getShowtimeStartAtSnapshot()));
                        String seats = b.getSeatCodes() != null ? String.join(", ", b.getSeatCodes()) : "";
                        sb.append("- Vé #").append(b.getBookingId())
                          .append(": ").append(b.getMovieTitleSnapshot())
                          .append("\n  Suất: ").append(timeStr)
                          .append(" | Ghế: ").append(seats)
                          .append(" | Trạng thái: ").append(b.getBookingStatus()).append("\n");
                    }
                    sb.append("Xem chi tiết vé và mã QR trong mục Vé đã đặt.");
                    replyContent = sb.toString().trim();
                }
            } catch (Exception e) {
                log.error("Error fetching user bookings: ", e);
                replyContent = "Không thể truy xuất lịch sử đặt vé của bạn lúc này.";
            }
            matchedIntent = true;
        } else if (matchesAny(content, refundKws)) {
            replyContent = "CineBot ghi nhận yêu cầu hoàn tiền/hủy vé của bạn. Theo chính sách của rạp, vé đã mua thành công không được hỗ trợ hoàn/hủy trực tuyến. Vui lòng liên hệ Hotline 1900 xxxx hoặc đến trực tiếp quầy vé trước giờ chiếu ít nhất 30 phút để được hỗ trợ theo quy định.";
            matchedIntent = true;
        } else if (matchesAny(content, accountKws)) {
            replyContent = "Để quản lý tài khoản, cập nhật thông tin cá nhân hoặc đổi mật khẩu, bạn vui lòng chọn mục 'Cá nhân' ở thanh điều hướng dưới cùng của ứng dụng. Để đăng nhập, hãy nhấn nút 'Đăng nhập' ở góc trên cùng của màn hình Cá nhân.";
            matchedIntent = true;
        }

        if (!matchedIntent) {
            replyContent = "CineBot chưa hỗ trợ thông tin này.\n" +
                    "Vui lòng liên hệ Hotline 1900 xxxx hoặc đến quầy vé tại rạp để được hỗ trợ nhanh nhất.\n" +
                    "Bạn có thể hỏi về:\n" +
                    "- Phim đang chiếu, Suất chiếu hôm nay, Địa chỉ rạp\n" +
                    "- Giá vé, Vé của tôi, Voucher của tôi, Thanh toán";
        }

        ChatMessage botMessage = ChatMessage.builder()
                .convoId(convo.getConvoId())
                .senderId("SUPPORT_BOT")
                .receiverId(customerId)
                .content(replyContent)
                .sentAt(now)
                .build();
        
        DocumentReference msgRef = firestore.collection(ChatMessage.COLLECTION_NAME).document();
        botMessage.setMessageId(msgRef.getId());
        msgRef.set(botMessage).get();
        
        conversationService.updateConversationAfterMessage(convo.getConvoId(), botMessage, customerId, now);
    }

    public void clearConversationMessages(String convoId) throws ExecutionException, InterruptedException {
        // Query all messages in the conversation
        List<QueryDocumentSnapshot> msgDocs = firestore.collection(ChatMessage.COLLECTION_NAME)
                .whereEqualTo("convoId", convoId)
                .get().get().getDocuments();

        // Delete each message in batch
        WriteBatch batch = firestore.batch();
        for (QueryDocumentSnapshot doc : msgDocs) {
            batch.delete(doc.getReference());
        }
        batch.commit().get();

        // Update the conversation lastMessage to null
        firestore.collection(Conversation.COLLECTION_NAME).document(convoId)
                .update("lastMessage", null, "lastMessageAt", null).get();
    }
}
