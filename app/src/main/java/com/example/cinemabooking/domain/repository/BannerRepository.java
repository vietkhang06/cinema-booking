package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Banner;

import java.util.List;

public interface BannerRepository {
    void getAllBanners(ResultCallback<List<Banner>> callback);
}