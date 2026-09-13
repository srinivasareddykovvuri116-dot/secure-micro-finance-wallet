package com.finance.wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AnalyticsTrendResponse {

    private LocalDate date;
    private BigDecimal inflow;
    private BigDecimal outflow;

    public AnalyticsTrendResponse(
            LocalDate date,
            BigDecimal inflow,
            BigDecimal outflow) {

        this.date = date;
        this.inflow = inflow;
        this.outflow = outflow;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getInflow() {
        return inflow;
    }

    public BigDecimal getOutflow() {
        return outflow;
    }
}