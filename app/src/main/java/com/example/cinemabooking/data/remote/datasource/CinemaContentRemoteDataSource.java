package com.example.cinemabooking.data.remote.datasource;

import com.example.cinemabookingapp.core.constants.FirestoreCollections;
import com.example.cinemabookingapp.domain.common.ResultCallback;
import com.example.cinemabookingapp.domain.model.Cinema_DienAnh.CinemaContent;
import com.example.cinemabookingapp.domain.model.Cinema_DienAnh.CinemaContentType;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CinemaContentRemoteDataSource {

    private final FirebaseFirestore firestore;

    public CinemaContentRemoteDataSource() {
        this(FirebaseFirestore.getInstance());
    }

    public CinemaContentRemoteDataSource(FirebaseFirestore firestore) {
        this.firestore = firestore;
    }

    public void getAll(ResultCallback<List<CinemaContent>> callback) {
        firestore.collection(FirestoreCollections.CINEMA_CONTENTS)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<CinemaContent> contents = new ArrayList<>();
                    for (DocumentSnapshot document : snapshot.getDocuments()) {
                        if (!isVisible(document)) {
                            continue;
                        }

                        CinemaContent item = mapSnapshot(document);
                        if (item != null) {
                            contents.add(item);
                        }
                    }

                    contents.sort((a, b) -> Long.compare(b.createdAt, a.createdAt));
                    if (callback != null) {
                        callback.onSuccess(contents);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(messageOrDefault(e, "Không thể tải nội dung điện ảnh"));
                    }
                });
    }

    public void getById(String id, ResultCallback<CinemaContent> callback) {
        if (id == null || id.trim().isEmpty()) {
            if (callback != null) callback.onError("contentId không hợp lệ");
            return;
        }

        firestore.collection(FirestoreCollections.CINEMA_CONTENTS)
                .document(id)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists() || !isVisible(document)) {
                        if (callback != null) callback.onError("Không tìm thấy nội dung");
                        return;
                    }

                    if (callback != null) {
                        callback.onSuccess(mapSnapshot(document));
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(messageOrDefault(e, "Không thể tải nội dung điện ảnh"));
                    }
                });
    }

    private CinemaContent mapSnapshot(DocumentSnapshot document) {
        Map<String, Object> data = document.getData();
        CinemaContent item = new CinemaContent();
        item.id = firstNonEmpty(readString(data, "id", "contentId"), document.getId());
        item.type = parseType(readString(data, "type", "contentType", "category"));
        item.tag = readString(data, "tag", "label");
        item.title = readString(data, "title", "name");
        item.excerpt = readString(data, "excerpt", "summary", "description", "shortDescription");
        item.content = readString(data, "content", "body", "detail", "fullContent");
        item.author = readString(data, "author", "createdBy", "writer");
        item.meta = readString(data, "meta", "subtitle");
        item.imageUrl = readString(data, "imageUrl", "coverUrl", "coverImageUrl", "thumbnailUrl", "posterUrl");
        item.createdAt = readLong(data, "createdAt", "updatedAt");

        if (item.type == null) {
            item.type = CinemaContentType.NEWS;
        }
        if (item.createdAt <= 0L) {
            item.createdAt = System.currentTimeMillis();
        }
        return item;
    }

    private boolean isVisible(DocumentSnapshot document) {
        Boolean deleted = document.getBoolean("deleted");
        if (deleted != null && deleted) {
            return false;
        }

        Boolean active = document.getBoolean("active");
        if (active == null) {
            active = document.getBoolean("isActive");
        }
        return active == null || active;
    }

    private CinemaContentType parseType(String raw) {
        if (raw == null) return null;

        String key = raw.trim().toUpperCase(Locale.ROOT);
        if ("COMMENT".equals(key) || "REVIEW".equals(key) || "COMMENTS".equals(key)) {
            return CinemaContentType.COMMENT;
        }
        if ("NEWS".equals(key) || "ARTICLE".equals(key)) {
            return CinemaContentType.NEWS;
        }
        if ("PERSON".equals(key) || "PEOPLE".equals(key) || "CAST".equals(key)) {
            return CinemaContentType.PERSON;
        }
        return null;
    }

    private String readString(Map<String, Object> data, String... keys) {
        if (data == null || keys == null) return "";
        for (String key : keys) {
            Object value = data.get(key);
            if (value == null) continue;
            String text = String.valueOf(value).trim();
            if (!text.isEmpty()) return text;
        }
        return "";
    }

    private long readLong(Map<String, Object> data, String... keys) {
        if (data == null || keys == null) return 0L;
        for (String key : keys) {
            Object value = data.get(key);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            if (value instanceof Timestamp) {
                return ((Timestamp) value).toDate().getTime();
            }
            if (value instanceof Date) {
                return ((Date) value).getTime();
            }
            if (value != null) {
                try {
                    return Long.parseLong(String.valueOf(value));
                } catch (Exception ignored) {
                }
            }
        }
        return 0L;
    }

    private String firstNonEmpty(String first, String second) {
        return first == null || first.trim().isEmpty() ? second : first;
    }

    private String messageOrDefault(Exception e, String defaultMessage) {
        if (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty()) {
            return e.getMessage();
        }
        return defaultMessage;
    }
}
