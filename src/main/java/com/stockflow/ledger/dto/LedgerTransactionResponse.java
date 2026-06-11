package com.stockflow.ledger.dto;

import com.stockflow.ledger.LedgerSourceType;
import com.stockflow.ledger.LedgerTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LedgerTransactionResponse(
        Long id,
        LedgerTransactionType type,
        BigDecimal amount,
        String currency,
        String description,
        LedgerSourceType sourceType,
        Long sourceId,
        LocalDateTime createdAt
) {
}
