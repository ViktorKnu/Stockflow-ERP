package com.stockflow.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 80) String sku,
        @Size(max = 1000) String description,
        @NotBlank @Size(max = 120) String category,
        @NotNull @PositiveOrZero Integer quantity,
        @NotNull @PositiveOrZero Integer minimumStock,
        @NotNull @Positive BigDecimal price,
        Long supplierId
) {
}
