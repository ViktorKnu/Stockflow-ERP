package com.stockflow.purchaseorder.dto;

import jakarta.validation.constraints.NotNull;

public record PurchaseOrderCreateRequest(
        @NotNull Long supplierId
) {
}
