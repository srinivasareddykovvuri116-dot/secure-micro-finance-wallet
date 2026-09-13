package com.finance.wallet.dto;

import java.time.Instant;

public class AuditLogResponse {

    private Long id;
    private Long userId;
    private String action;
    private String details;
    private Instant createdAt;

    public AuditLogResponse(
            Long id,
            Long userId,
            String action,
            String details,
            Instant createdAt) {

        this.id = id;
        this.userId = userId;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}