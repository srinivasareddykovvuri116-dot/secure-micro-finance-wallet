package com.finance.wallet.dto;

import java.math.BigDecimal;

public class AnalyticsResponse {

    private BigDecimal currentBalance;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal totalSent;
    private BigDecimal totalReceived;

    private long depositCount;
    private long withdrawalCount;
    private long sentCount;
    private long receivedCount;

    public AnalyticsResponse(
            BigDecimal currentBalance,
            BigDecimal totalDeposits,
            BigDecimal totalWithdrawals,
            BigDecimal totalSent,
            BigDecimal totalReceived,
            long depositCount,
            long withdrawalCount,
            long sentCount,
            long receivedCount) {

        this.currentBalance = currentBalance;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.totalSent = totalSent;
        this.totalReceived = totalReceived;
        this.depositCount = depositCount;
        this.withdrawalCount = withdrawalCount;
        this.sentCount = sentCount;
        this.receivedCount = receivedCount;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getTotalDeposits() {
        return totalDeposits;
    }

    public BigDecimal getTotalWithdrawals() {
        return totalWithdrawals;
    }

    public BigDecimal getTotalSent() {
        return totalSent;
    }

    public BigDecimal getTotalReceived() {
        return totalReceived;
    }

    public long getDepositCount() {
        return depositCount;
    }

    public long getWithdrawalCount() {
        return withdrawalCount;
    }

    public long getSentCount() {
        return sentCount;
    }

    public long getReceivedCount() {
        return receivedCount;
    }
}