package com.stockflow.product;

import com.stockflow.product.dto.ProductResponse;
import com.stockflow.product.dto.SupplierSummaryResponse;
import com.stockflow.supplier.Supplier;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static ProductResponse toResponse(Product product) {
        Supplier supplier = product.getSupplier();
        SupplierSummaryResponse supplierResponse = supplier == null
                ? null
                : new SupplierSummaryResponse(supplier.getId(), supplier.getName());

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getCategory(),
                product.getQuantity(),
                product.getMinimumStock(),
                product.getPrice(),
                product.isLowStock(),
                supplierResponse,
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
