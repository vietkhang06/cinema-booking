package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.SnackCategory;

import java.util.List;

public interface SnackCategoryRepository {
    void createCategory(SnackCategory category, ResultCallback<SnackCategory> callback);
    void getCategoryById(String categoryId, ResultCallback<SnackCategory> callback);
    void getAllCategories(ResultCallback<List<SnackCategory>> callback);
    void updateCategory(SnackCategory category, ResultCallback<SnackCategory> callback);
    void softDeleteCategory(String categoryId, ResultCallback<Void> callback);
}