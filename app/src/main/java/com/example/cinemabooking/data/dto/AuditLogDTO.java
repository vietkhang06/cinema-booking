package com.example.cinemabooking.data.dto;

public class AuditLogDTO {
    public String logId;
    public String actorId;
    public String actorRole;
    public String action;
    public String targetType;
    public String targetId;
    public String note;
    public String adminId;
    public String showtimeId;
    public int bookingCount;
    public long createdAt;

    public AuditLogDTO() {
    }
}