package com.stockflow.supplier.dto;

import java.time.LocalDateTime;

public record SupplierResponse(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
