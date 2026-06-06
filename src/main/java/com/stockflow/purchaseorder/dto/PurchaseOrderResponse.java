package com.stockflow.purchaseorder.dto;

import com.stockflow.purchaseorder.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        Long supplierId,
        String supplierName,
        PurchaseOrderStatus status,
        BigDecimal totalAmount,
        List<PurchaseOrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
