package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Snack;

import java.util.List;

public interface SnackRepository {
    void createSnack(Snack snack, ResultCallback<Snack> callback);
    void getSnackById(String snackId, ResultCallback<Snack> callback);
    void getAllSnacks(ResultCallback<List<Snack>> callback);
    void getSnacksByCategoryId(String categoryId, ResultCallback<List<Snack>> callback);
    void updateSnack(Snack snack, ResultCallback<Snack> callback);
    void setAvailability(String snackId, boolean available, ResultCallback<Snack> callback);
    void softDeleteSnack(String snackId, ResultCallback<Void> callback);
}