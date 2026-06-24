package com.stockflow.salesorder;

import com.stockflow.product.Product;
import com.stockflow.salesorder.dto.SalesOrderItemResponse;
import com.stockflow.salesorder.dto.SalesOrderResponse;

public final class SalesOrderMapper {

    private SalesOrderMapper() {
    }

    public static SalesOrderResponse toResponse(SalesOrder order) {
        return new SalesOrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getItems().stream()
                        .map(SalesOrderMapper::toItemResponse)
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private static SalesOrderItemResponse toItemResponse(SalesOrderItem item) {
        Product product = item.getProduct();
        return new SalesOrderItemResponse(
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
