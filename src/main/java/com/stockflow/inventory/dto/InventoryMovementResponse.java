package com.stockflow.inventory.dto;

import com.stockflow.inventory.MovementType;

import java.time.LocalDateTime;

public record InventoryMovementResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        MovementType type,
        Integer quantity,
        String reason,
        Integer previousQuantity,
        Integer newQuantity,
        LocalDateTime createdAt
) {
}
