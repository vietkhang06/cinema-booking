package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Promotion;

import java.util.List;

public interface PromotionRepository {
    void createPromotion(Promotion promotion, ResultCallback<Promotion> callback);
    void getPromotionById(String promoId, ResultCallback<Promotion> callback);
    void getAllPromotions(ResultCallback<List<Promotion>> callback);
    void getActivePromotions(long currentTime, ResultCallback<List<Promotion>> callback);
    void getPromotionByCode(String code, ResultCallback<Promotion> callback);
    void updatePromotion(Promotion promotion, ResultCallback<Promotion> callback);
    void softDeletePromotion(String promoId, ResultCallback<Void> callback);
    void increaseUsageCount(String promoId, ResultCallback<Promotion> callback);
}