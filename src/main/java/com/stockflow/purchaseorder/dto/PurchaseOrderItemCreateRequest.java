package com.stockflow.purchaseorder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PurchaseOrderItemCreateRequest(
        @NotNull Long productId,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal unitPrice
) {
}
