package com.stockflow.salesorder;

import com.stockflow.exception.BusinessRuleException;
import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.product.Product;
import com.stockflow.product.ProductRepository;
import com.stockflow.salesorder.dto.SalesOrderCreateRequest;
import com.stockflow.salesorder.dto.SalesOrderItemCreateRequest;
import com.stockflow.salesorder.dto.SalesOrderResponse;
import com.stockflow.salesorder.dto.SalesOrderStatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<SalesOrderResponse> findAll() {
        return salesOrderRepository.findAll().stream()
                .map(SalesOrderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalesOrderResponse findById(Long id) {
        return SalesOrderMapper.toResponse(getSalesOrder(id));
    }

    @Transactional
    public SalesOrderResponse create(SalesOrderCreateRequest request) {
        SalesOrder order = SalesOrder.builder()
                .customerName(request.customerName())
                .customerEmail(request.customerEmail())
                .status(SalesOrderStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .build();

        return SalesOrderMapper.toResponse(salesOrderRepository.save(order));
    }

    @Transactional
    public SalesOrderResponse addItem(Long orderId, SalesOrderItemCreateRequest request) {
        SalesOrder order = getSalesOrder(orderId);
        ensureDraft(order);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));

        SalesOrderItem item = SalesOrderItem.builder()
                .product(product)
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .lineTotal(request.unitPrice().multiply(BigDecimal.valueOf(request.quantity())))
                .build();

        order.addItem(item);
        return SalesOrderMapper.toResponse(order);
    }

    @Transactional
    public SalesOrderResponse updateStatus(Long id, SalesOrderStatusUpdateRequest request) {
        SalesOrder order = getSalesOrder(id);
        SalesOrderStatus newStatus = request.status();

        if (newStatus == SalesOrderStatus.SHIPPED) {
            throw new BusinessRuleException("Use the ship workflow to mark a sales order as shipped");
        }
        if (order.getStatus() == SalesOrderStatus.CANCELLED) {
            throw new BusinessRuleException("Cancelled sales orders cannot change status");
        }
        if (order.getStatus() == SalesOrderStatus.SHIPPED) {
            throw new BusinessRuleException("Shipped sales orders cannot change status");
        }
        if (newStatus == SalesOrderStatus.CONFIRMED) {
            ensureHasItems(order);
            ensureEnoughStock(order);
        }
        if (newStatus == SalesOrderStatus.PAID && order.getStatus() != SalesOrderStatus.CONFIRMED) {
            throw new BusinessRuleException("Sales order must be CONFIRMED before it can be marked as PAID");
        }

        order.setStatus(newStatus);
        return SalesOrderMapper.toResponse(order);
    }

    @Transactional
    public void delete(Long id) {
        SalesOrder order = getSalesOrder(id);
        if (order.getStatus() == SalesOrderStatus.SHIPPED) {
            throw new BusinessRuleException("Shipped sales orders cannot be deleted");
        }
        salesOrderRepository.delete(order);
    }

    private SalesOrder getSalesOrder(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales order not found: " + id));
    }

    private void ensureDraft(SalesOrder order) {
        if (order.getStatus() != SalesOrderStatus.DRAFT) {
            throw new BusinessRuleException("Sales order items can only be changed while status is DRAFT");
        }
    }

    private void ensureHasItems(SalesOrder order) {
        if (order.getItems().isEmpty()) {
            throw new BusinessRuleException("Sales order must have at least one item before it can be confirmed");
        }
    }

    private void ensureEnoughStock(SalesOrder order) {
        for (SalesOrderItem item : order.getItems()) {
            Product product = item.getProduct();
            if (product.getQuantity() < item.getQuantity()) {
                throw new BusinessRuleException("Not enough stock for product: " + product.getId());
            }
        }
    }
}
