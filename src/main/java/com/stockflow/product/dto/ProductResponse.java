package com.stockflow.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        String description,
        String category,
        Integer quantity,
        Integer minimumStock,
        BigDecimal price,
        boolean lowStock,
        SupplierSummaryResponse supplier,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
