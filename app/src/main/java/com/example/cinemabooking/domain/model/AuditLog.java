package com.example.cinemabooking.domain.model;

public class AuditLog {
    public String logId;
    public String actorId;
    public String actorRole;
    public String action;
    public String targetType;
    public String targetId;
    public String note;
    public long createdAt;

    public AuditLog() {
    }
}