package com.stockflow.inventory;

import com.stockflow.inventory.dto.InventoryMovementResponse;
import com.stockflow.product.Product;

public final class InventoryMovementMapper {

    private InventoryMovementMapper() {
    }

    public static InventoryMovementResponse toResponse(InventoryMovement movement) {
        Product product = movement.getProduct();
        return new InventoryMovementResponse(
                movement.getId(),
                product.getId(),
                product.getName(),
                product.getSku(),
                movement.getType(),
                movement.getQuantity(),
                movement.getReason(),
                movement.getPreviousQuantity(),
                movement.getNewQuantity(),
                movement.getCreatedAt()
        );
    }
}
