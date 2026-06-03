package com.stockflow.supplier;

import com.stockflow.supplier.dto.SupplierResponse;

public final class SupplierMapper {

    private SupplierMapper() {
    }

    public static SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getEmail(),
                supplier.getPhone(),
                supplier.getAddress(),
                supplier.getCreatedAt(),
                supplier.getUpdatedAt()
        );
    }
}
