package com.stockflow.salesorder.dto;

import com.stockflow.salesorder.SalesOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SalesOrderResponse(
        Long id,
        String customerName,
        String customerEmail,
        SalesOrderStatus status,
        BigDecimal totalAmount,
        List<SalesOrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
