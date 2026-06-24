package com.stockflow.salesorder.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record SalesOrderItemCreateRequest(
        @NotNull Long productId,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal unitPrice
) {
}
