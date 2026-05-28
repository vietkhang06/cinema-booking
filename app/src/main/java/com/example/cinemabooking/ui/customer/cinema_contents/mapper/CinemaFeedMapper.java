package com.example.cinemabooking.ui.customer.cinema_contents.mapper;

import com.example.cinemabooking.domain.model.Cinema_DienAnh.CinemaContent;
import com.example.cinemabooking.ui.customer.cinema_contents.model.CinemaFeedItem;

import java.util.ArrayList;
import java.util.List;

public final class CinemaFeedMapper {

    private CinemaFeedMapper() {
    }

    public static CinemaFeedItem toFeedItem(CinemaContent src) {
        if (src == null) return null;

        CinemaFeedItem item = new CinemaFeedItem();
        item.id = src.id;
        item.type = src.type;
        item.tag = src.tag;
        item.title = src.title;
        item.excerpt = src.excerpt;
        item.content = src.content;
        item.author = src.author;
        item.meta = src.meta;
        item.imageUrl = src.imageUrl;
        return item;
    }

    public static List<CinemaFeedItem> toFeedItems(List<CinemaContent> src) {
        List<CinemaFeedItem> result = new ArrayList<>();
        if (src == null) return result;

        for (CinemaContent content : src) {
            CinemaFeedItem item = toFeedItem(content);
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }
}