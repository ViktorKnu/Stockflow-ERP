package com.stockflow.purchaseorder.dto;

import java.math.BigDecimal;

public record PurchaseOrderItemResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
) {
}
