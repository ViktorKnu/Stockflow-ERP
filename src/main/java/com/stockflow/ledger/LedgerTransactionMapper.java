package com.stockflow.ledger;

import com.stockflow.ledger.dto.LedgerTransactionResponse;

public final class LedgerTransactionMapper {

    private LedgerTransactionMapper() {
    }

    public static LedgerTransactionResponse toResponse(LedgerTransaction transaction) {
        return new LedgerTransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getSourceType(),
                transaction.getSourceId(),
                transaction.getCreatedAt()
        );
    }
}
