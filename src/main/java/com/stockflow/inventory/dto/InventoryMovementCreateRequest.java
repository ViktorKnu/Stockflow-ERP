package com.stockflow.inventory.dto;

import com.stockflow.inventory.MovementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record InventoryMovementCreateRequest(
        @NotNull Long productId,
        @NotNull MovementType type,
        @NotNull @PositiveOrZero Integer quantity,
        @NotBlank @Size(max = 500) String reason
) {
}
