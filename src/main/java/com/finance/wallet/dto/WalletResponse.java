package com.finance.wallet.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class WalletResponse {

    private Long walletId;
    private BigDecimal balance;
    private Instant createdAt;
    private Instant updatedAt;

    public WalletResponse(
            Long walletId,
            BigDecimal balance,
            Instant createdAt,
            Instant updatedAt) {

        this.walletId = walletId;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getWalletId() {
        return walletId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}