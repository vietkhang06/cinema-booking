package com.example.cinemabooking.ui.customer.cinema_contents.model;

import com.example.cinemabooking.domain.model.Cinema_DienAnh.CinemaContentType;

public class CinemaFeedItem {
    public String id;
    public CinemaContentType type;
    public String tag;
    public String title;
    public String excerpt;
    public String content;
    public String author;
    public String meta;
    public String imageUrl;

    public CinemaFeedItem() {
    }
}