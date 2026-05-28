package com.example.cinemabooking.domain.usecase.movie.banner;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.Banner;
import com.example.cinemabooking.domain.repository.BannerRepository;

import java.util.List;

public class GetBannersUseCase {

    private final BannerRepository repository;

    public GetBannersUseCase(BannerRepository repository) {
        this.repository = repository;
    }

    public void execute(ResultCallback<List<Banner>> callback) {
        repository.getAllBanners(callback);
    }
}