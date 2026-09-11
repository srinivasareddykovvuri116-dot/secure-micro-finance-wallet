package com.finance.wallet.dto;

import com.finance.wallet.entity.TransactionStatus;
import com.finance.wallet.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionResponse {

    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
    private String referenceId;
    private Instant createdAt;

    public TransactionResponse(
            Long id,
            TransactionType type,
            BigDecimal amount,
            TransactionStatus status,
            String referenceId,
            Instant createdAt) {

        this.id = id;
        this.type = type;
        this.amount = amount;
        this.status = status;
        this.referenceId = referenceId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}