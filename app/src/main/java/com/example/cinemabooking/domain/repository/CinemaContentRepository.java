package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema_DienAnh.CinemaContent;
import com.example.cinemabooking.domain.model.Cinema_DienAnh.CinemaContentType;

import java.util.List;
public interface CinemaContentRepository {
    void getAll(ResultCallback<List<CinemaContent>> callback);
    void getByType(CinemaContentType type, ResultCallback<List<CinemaContent>> callback);
    void search(String query, ResultCallback<List<CinemaContent>> callback);
    void getById(String id, ResultCallback<CinemaContent> callback);
}
