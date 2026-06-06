package com.stockflow.purchaseorder;

import com.stockflow.product.Product;
import com.stockflow.purchaseorder.dto.PurchaseOrderItemResponse;
import com.stockflow.purchaseorder.dto.PurchaseOrderResponse;
import com.stockflow.supplier.Supplier;

public final class PurchaseOrderMapper {

    private PurchaseOrderMapper() {
    }

    public static PurchaseOrderResponse toResponse(PurchaseOrder order) {
        Supplier supplier = order.getSupplier();
        return new PurchaseOrderResponse(
                order.getId(),
                supplier.getId(),
                supplier.getName(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getItems().stream()
                        .map(PurchaseOrderMapper::toItemResponse)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private static PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item) {
        Product product = item.getProduct();
        return new PurchaseOrderItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                product.getSku(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        );
    }
}
