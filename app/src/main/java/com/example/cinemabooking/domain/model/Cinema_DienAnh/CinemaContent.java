package com.example.cinemabooking.domain.model.Cinema_DienAnh;

public class CinemaContent {
    public String id;
    public CinemaContentType type;
    public String tag;
    public String title;
    public String excerpt;
    public String content;
    public String author;
    public String meta;
    public String imageUrl;
    public long createdAt;

    public CinemaContent() {
    }

    public CinemaContent(String id, CinemaContentType type, String tag, String title,
                         String excerpt, String content, String author,
                         String meta, String imageUrl, long createdAt) {
        this.id = id;
        this.type = type;
        this.tag = tag;
        this.title = title;
        this.excerpt = excerpt;
        this.content = content;
        this.author = author;
        this.meta = meta;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }
}