package com.stockflow.salesorder.dto;

import com.stockflow.salesorder.SalesOrderStatus;
import jakarta.validation.constraints.NotNull;

public record SalesOrderStatusUpdateRequest(
        @NotNull SalesOrderStatus status
) {
}
