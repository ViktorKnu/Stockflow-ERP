package com.stockflow.purchaseorder.dto;

import com.stockflow.purchaseorder.PurchaseOrderStatus;
import jakarta.validation.constraints.NotNull;

public record PurchaseOrderStatusUpdateRequest(
        @NotNull PurchaseOrderStatus status
) {
}
