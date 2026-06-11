package com.stockflow.purchaseorder;

import com.stockflow.exception.BusinessRuleException;
import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.inventory.InventoryMovementService;
import com.stockflow.inventory.MovementType;
import com.stockflow.ledger.LedgerService;
import com.stockflow.ledger.LedgerSourceType;
import com.stockflow.product.Product;
import com.stockflow.product.ProductRepository;
import com.stockflow.purchaseorder.dto.PurchaseOrderCreateRequest;
import com.stockflow.purchaseorder.dto.PurchaseOrderItemCreateRequest;
import com.stockflow.purchaseorder.dto.PurchaseOrderResponse;
import com.stockflow.purchaseorder.dto.PurchaseOrderStatusUpdateRequest;
import com.stockflow.supplier.Supplier;
import com.stockflow.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementService inventoryMovementService;
    private final LedgerService ledgerService;

    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> findAll() {
        return purchaseOrderRepository.findAll().stream()
                .map(PurchaseOrderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseOrderResponse findById(Long id) {
        return PurchaseOrderMapper.toResponse(getPurchaseOrder(id));
    }

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderCreateRequest request) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.supplierId()));

        PurchaseOrder order = PurchaseOrder.builder()
                .supplier(supplier)
                .status(PurchaseOrderStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .build();

        return PurchaseOrderMapper.toResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse addItem(Long orderId, PurchaseOrderItemCreateRequest request) {
        PurchaseOrder order = getPurchaseOrder(orderId);
        ensureDraft(order);

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + request.productId()));

        PurchaseOrderItem item = PurchaseOrderItem.builder()
                .product(product)
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .lineTotal(request.unitPrice().multiply(BigDecimal.valueOf(request.quantity())))
                .build();

        order.addItem(item);
        return PurchaseOrderMapper.toResponse(order);
    }

    @Transactional
    public PurchaseOrderResponse updateStatus(Long id, PurchaseOrderStatusUpdateRequest request) {
        PurchaseOrder order = getPurchaseOrder(id);
        PurchaseOrderStatus newStatus = request.status();

        if (newStatus == PurchaseOrderStatus.RECEIVED) {
            throw new BusinessRuleException("Use the receive workflow to mark a purchase order as received");
        }
        if (order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BusinessRuleException("Cancelled purchase orders cannot change status");
        }
        if (newStatus == PurchaseOrderStatus.ORDERED && order.getItems().isEmpty()) {
            throw new BusinessRuleException("Purchase order must have at least one item before it can be ordered");
        }

        order.setStatus(newStatus);
        return PurchaseOrderMapper.toResponse(order);
    }

    @Transactional
    public PurchaseOrderResponse receive(Long id) {
        PurchaseOrder order = getPurchaseOrder(id);

        if (order.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new BusinessRuleException("Purchase order has already been received");
        }
        if (order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BusinessRuleException("Cancelled purchase orders cannot be received");
        }
        if (order.getStatus() != PurchaseOrderStatus.ORDERED) {
            throw new BusinessRuleException("Only ORDERED purchase orders can be received");
        }

        for (PurchaseOrderItem item : order.getItems()) {
            inventoryMovementService.recordMovement(
                    item.getProduct(),
                    MovementType.IN,
                    item.getQuantity(),
                    "Purchase order received: " + order.getId()
            );
        }

        ledgerService.recordExpense(
                order.getTotalAmount(),
                "Purchase order received: " + order.getId(),
                LedgerSourceType.PURCHASE_ORDER,
                order.getId()
        );

        order.setStatus(PurchaseOrderStatus.RECEIVED);
        return PurchaseOrderMapper.toResponse(order);
    }

    @Transactional
    public void delete(Long id) {
        PurchaseOrder order = getPurchaseOrder(id);
        if (order.getStatus() == PurchaseOrderStatus.RECEIVED) {
            throw new BusinessRuleException("Received purchase orders cannot be deleted");
        }
        purchaseOrderRepository.delete(order);
    }

    private PurchaseOrder getPurchaseOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + id));
    }

    private void ensureDraft(PurchaseOrder order) {
        if (order.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new BusinessRuleException("Purchase order items can only be changed while status is DRAFT");
        }
    }
}
