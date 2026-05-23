package com.example.cinemabooking.domain.repository;

import com.example.cinemabooking.domain.common.ResultCallback;
import com.example.cinemabooking.domain.model.AuditLog;

import java.util.List;

public interface AuditLogRepository {
    void createLog(AuditLog log, ResultCallback<AuditLog> callback);
    void getAllLogs(ResultCallback<List<AuditLog>> callback);
    void getLogsByActorId(String actorId, ResultCallback<List<AuditLog>> callback);
    void getLogsByTargetId(String targetId, ResultCallback<List<AuditLog>> callback);
}