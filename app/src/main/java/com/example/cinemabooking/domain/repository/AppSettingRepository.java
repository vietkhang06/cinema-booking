package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.AppSetting;

import java.util.List;

public interface AppSettingRepository {
    void upsertSetting(AppSetting setting, ResultCallback<AppSetting> callback);
    void getSettingByKey(String key, ResultCallback<AppSetting> callback);
    void getAllSettings(ResultCallback<List<AppSetting>> callback);
}