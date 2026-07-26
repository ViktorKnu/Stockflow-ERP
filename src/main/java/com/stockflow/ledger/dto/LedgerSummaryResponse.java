package com.stockflow.ledger.dto;

import java.math.BigDecimal;

public record LedgerSummaryResponse(
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal totalRefunds,
        BigDecimal totalAdjustments,
        BigDecimal netProfit,
        long transactionCount
) {
}
