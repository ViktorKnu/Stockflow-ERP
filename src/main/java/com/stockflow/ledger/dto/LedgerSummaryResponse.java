package com.stockflow.ledger.dto;

import java.math.BigDecimal;

public record LedgerSummaryResponse(
        BigDecimal totalRevenue,
        BigDecimal totalExpenses,
        BigDecimal netProfit,
        long transactionCount
) {
}
