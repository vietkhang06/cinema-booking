package com.example.cinemabooking.data.repository;

import com.example.cinemabooking.data.local.Cinema_DienAnh.CinemaContentMockDataSource;
import com.example.cinemabooking.data.remote.datasource.CinemaContentRemoteDataSource;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Cinema_DienAnh.CinemaContent;
import com.example.cinemabooking.domain.model.Cinema_DienAnh.CinemaContentType;
import com.example.cinemabooking.domain.repository.CinemaContentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CinemaContentRepositoryImpl implements CinemaContentRepository {

    private final CinemaContentMockDataSource mockDataSource = new CinemaContentMockDataSource();
    private final CinemaContentRemoteDataSource remoteDataSource = new CinemaContentRemoteDataSource();

    @Override
    public void getAll(ResultCallback<List<CinemaContent>> callback) {
        remoteDataSource.getAll(new ResultCallback<List<CinemaContent>>() {
            @Override
            public void onSuccess(List<CinemaContent> data) {
                if (data == null || data.isEmpty()) {
                    callback.onSuccess(mockDataSource.getAll());
                } else {
                    callback.onSuccess(data);
                }
            }

            @Override
            public void onError(String message) {
                callback.onSuccess(mockDataSource.getAll());
            }
        });
    }

    @Override
    public void getByType(CinemaContentType type, ResultCallback<List<CinemaContent>> callback) {
        getAll(new ResultCallback<List<CinemaContent>>() {
            @Override
            public void onSuccess(List<CinemaContent> data) {
                List<CinemaContent> result = new ArrayList<>();
                if (data != null && type != null) {
                    for (CinemaContent item : data) {
                        if (item != null && item.type == type) {
                            result.add(item);
                        }
                    }
                }
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void search(String query, ResultCallback<List<CinemaContent>> callback) {
        getAll(new ResultCallback<List<CinemaContent>>() {
            @Override
            public void onSuccess(List<CinemaContent> data) {
                String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
                if (q.isEmpty()) {
                    callback.onSuccess(data);
                    return;
                }

                List<CinemaContent> result = new ArrayList<>();
                if (data != null) {
                    for (CinemaContent item : data) {
                        String haystack = safe(item.tag) + " " + safe(item.title) + " " + safe(item.excerpt) + " " + safe(item.meta) + " " + safe(item.content);
                        if (haystack.toLowerCase(Locale.ROOT).contains(q)) {
                            result.add(item);
                        }
                    }
                }
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void getById(String id, ResultCallback<CinemaContent> callback) {
        remoteDataSource.getById(id, new ResultCallback<CinemaContent>() {
            @Override
            public void onSuccess(CinemaContent data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                CinemaContent item = mockDataSource.getById(id);
                if (item == null) {
                    callback.onError("Không tìm thấy nội dung");
                } else {
                    callback.onSuccess(item);
                }
            }
        });
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
