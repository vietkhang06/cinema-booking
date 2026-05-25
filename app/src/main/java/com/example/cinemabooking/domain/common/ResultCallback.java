package com.example.cinemabooking.domain.common;

public interface ResultCallback<T> {
    void onSuccess(T data);
    void onError(String message);

}
