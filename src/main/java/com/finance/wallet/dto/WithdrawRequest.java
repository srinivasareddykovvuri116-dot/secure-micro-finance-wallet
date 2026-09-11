package com.finance.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class WithdrawRequest {

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    public WithdrawRequest() {
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}