package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.SnackOrder;

import java.util.List;

public interface SnackOrderRepository {
    void createSnackOrder(SnackOrder order, ResultCallback<SnackOrder> callback);
    void getSnackOrderById(String snackOrderId, ResultCallback<SnackOrder> callback);
    void getSnackOrdersByUserId(String userId, ResultCallback<List<SnackOrder>> callback);
    void getSnackOrdersByBookingId(String bookingId, ResultCallback<List<SnackOrder>> callback);
    void updateSnackOrderStatus(String snackOrderId, String status, ResultCallback<SnackOrder> callback);
    void softDeleteSnackOrder(String snackOrderId, ResultCallback<Void> callback);
}