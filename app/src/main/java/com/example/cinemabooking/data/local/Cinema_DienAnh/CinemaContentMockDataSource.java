package com.example.cinemabooking.data.local.Cinema_DienAnh;

import com.example.cinemabooking.domain.model.Cinema_DienAnh.CinemaContent;
import com.example.cinemabooking.domain.model.Cinema_DienAnh.CinemaContentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CinemaContentMockDataSource {

    private final List<CinemaContent> cache = new ArrayList<>();

    public CinemaContentMockDataSource() {
        long now = System.currentTimeMillis();

        cache.add(new CinemaContent(
                "cmt_1",
                CinemaContentType.COMMENT,
                "Bình luận",
                "(Review) Lửa anh hùng: Thái Hòa tạo điểm nhấn cảm xúc",
                "Bộ phim nổi bật nhờ nhịp kể chắc, các đoạn cao trào có lực và dàn diễn viên chính giàu cảm xúc.",
                "Bài viết phân tích diễn xuất, nhịp dựng và cách bộ phim xây dựng cảm xúc cho khán giả. Điểm mạnh nằm ở những cảnh đối thoại vừa đủ, phần hình ảnh giữ được không khí điện ảnh và tuyến nhân vật chính có động cơ rõ ràng.",
                "Ban biên tập",
                "Bình luận - 5 phút đọc",
                "https://picsum.photos/seed/cinema_review_1/900/600",
                now
        ));

        cache.add(new CinemaContent(
                "cmt_2",
                CinemaContentType.COMMENT,
                "Bình luận",
                "(Review) Trùm sò: Một thử nghiệm hài dễ xem trên màn rộng",
                "Bài review tập trung vào bố cục cảnh, tiết tấu hài và mức độ giải trí tổng thể.",
                "Phim chọn cách tiếp cận nhẹ nhàng, ưu tiên tiếng cười đời thường và các tình huống gần gũi. Dù chưa phải tác phẩm đột phá, nhịp phim đủ nhanh để giữ sự chú ý và tạo cảm giác dễ xem cho nhóm khán giả đại chúng.",
                "Ban biên tập",
                "Bình luận - 3 phút đọc",
                "https://picsum.photos/seed/cinema_review_2/900/600",
                now
        ));

        cache.add(new CinemaContent(
                "news_1",
                CinemaContentType.NEWS,
                "Tin mới",
                "Dòng phim kinh dị dân gian tiếp tục trở lại với chất liệu truyền thuyết Khmer",
                "Tin điện ảnh với thông tin hậu trường và định hướng sáng tạo của ê-kíp sản xuất.",
                "Dự án mới khai thác không khí huyền bí từ văn hóa dân gian, kết hợp bối cảnh địa phương với cách kể chuyện hiện đại. Ê-kíp cho biết phần hình ảnh và âm thanh sẽ được đầu tư để tạo cảm giác căng thẳng nhưng vẫn giữ nét riêng của chất liệu gốc.",
                "Cinema Desk",
                "Tin tức - vừa cập nhật",
                "https://picsum.photos/seed/cinema_news_1/900/600",
                now
        ));

        cache.add(new CinemaContent(
                "news_2",
                CinemaContentType.NEWS,
                "Tin mới",
                "Những phim được mong chờ nhất trong tháng",
                "Tổng hợp nhanh các tựa phim, thể loại và câu chuyện đang được khán giả quan tâm.",
                "Lịch chiếu tháng này có sự pha trộn giữa phim thương mại, hoạt hình gia đình và một vài tác phẩm tâm lý có màu sắc tác giả. Đây là nhóm phim đáng chú ý cho khán giả muốn chọn lịch xem cuối tuần.",
                "Cinema Desk",
                "Tin tức - 1 giờ trước",
                "https://picsum.photos/seed/cinema_news_2/900/600",
                now
        ));

        cache.add(new CinemaContent(
                "person_1",
                CinemaContentType.PERSON,
                "Diễn viên",
                "Chris Evans",
                "Không chỉ gắn với hình tượng hành động, anh thường tạo dấu ấn bằng lối diễn tiết chế và có chiều sâu cảm xúc.",
                "Hồ sơ nghệ sĩ nhìn lại các vai diễn nổi bật, phong cách lựa chọn kịch bản và sức hút của Chris Evans với khán giả đại chúng.",
                "Hồ sơ",
                "Gương mặt - nổi bật",
                "https://picsum.photos/seed/cinema_person_1/900/600",
                now
        ));

        cache.add(new CinemaContent(
                "person_2",
                CinemaContentType.PERSON,
                "Diễn viên",
                "Margot Robbie",
                "Một trong những gương mặt nổi bật của Hollywood với khả năng biến hóa tốt giữa phim thương mại và phim tác giả.",
                "Hồ sơ nghệ sĩ tóm tắt hành trình sự nghiệp, các vai diễn đáng nhớ và cách Margot Robbie xây dựng hình ảnh đa dạng trên màn ảnh.",
                "Hồ sơ",
                "Gương mặt - nổi bật",
                "https://picsum.photos/seed/cinema_person_2/900/600",
                now
        ));
    }

    public List<CinemaContent> getAll() {
        return new ArrayList<>(cache);
    }

    public CinemaContent getById(String id) {
        if (id == null) return null;
        for (CinemaContent item : cache) {
            if (id.equals(item.id)) return item;
        }
        return null;
    }

    public List<CinemaContent> getByType(CinemaContentType type) {
        List<CinemaContent> result = new ArrayList<>();
        if (type == null) return result;

        for (CinemaContent item : cache) {
            if (item.type == type) {
                result.add(item);
            }
        }
        return result;
    }

    public List<CinemaContent> search(String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return getAll();

        List<CinemaContent> result = new ArrayList<>();
        for (CinemaContent item : cache) {
            String haystack = safe(item.tag) + " " + safe(item.title) + " " + safe(item.excerpt) + " " + safe(item.meta) + " " + safe(item.content);
            if (haystack.toLowerCase(Locale.ROOT).contains(q)) {
                result.add(item);
            }
        }
        return result;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
