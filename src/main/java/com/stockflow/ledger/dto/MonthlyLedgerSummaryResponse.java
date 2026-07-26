package com.stockflow.ledger.dto;

import java.math.BigDecimal;

public record MonthlyLedgerSummaryResponse(
        String month,
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal totalRefunds,
        BigDecimal totalAdjustments,
        BigDecimal netProfit,
        long transactionCount
) {
}
