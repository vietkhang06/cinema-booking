package com.example.cinemabooking.data.repository;

import com.example.cinemabooking.data.remote.datasource.BannerRemoteDataSource;
import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Banner;
import com.example.cinemabooking.domain.repository.BannerRepository;

import java.util.List;

public class BannerRepositoryImpl implements BannerRepository {

    private final BannerRemoteDataSource remote;

    public BannerRepositoryImpl(BannerRemoteDataSource remote) {
        this.remote = remote;
    }

    @Override
    public void getAllBanners(ResultCallback<List<Banner>> callback) {
        remote.getAllBanners(callback);
    }
}