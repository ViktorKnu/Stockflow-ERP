package com.stockflow.purchaseorder;

import com.stockflow.audit.AuditAction;
import com.stockflow.audit.AuditLogService;
import com.stockflow.exception.BusinessRuleException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryMovementService inventoryMovementService;

    @Mock
    private LedgerService ledgerService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    @Test
    void canCreatePurchaseOrder() {
        Supplier supplier = supplier();
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder order = invocation.getArgument(0);
            order.setId(10L);
            return order;
        });

        PurchaseOrderResponse response = purchaseOrderService.create(new PurchaseOrderCreateRequest(1L));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.supplierId()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(PurchaseOrderStatus.DRAFT);
        assertThat(response.totalAmount()).isEqualByComparingTo("0.00");
        assertThat(response.items()).isEmpty();
    }

    @Test
    void canAddItemAndRecalculateTotalAmount() {
        PurchaseOrder order = purchaseOrder();
        Product product = product();
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));

        PurchaseOrderResponse response = purchaseOrderService.addItem(10L, new PurchaseOrderItemCreateRequest(
                20L,
                3,
                new BigDecimal("100.00")
        ));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().productId()).isEqualTo(20L);
        assertThat(response.items().getFirst().lineTotal()).isEqualByComparingTo("300.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("300.00");
    }

    @Test
    void cannotAddItemWhenOrderIsNotDraft() {
        PurchaseOrder order = purchaseOrder();
        order.setStatus(PurchaseOrderStatus.ORDERED);
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> purchaseOrderService.addItem(10L, new PurchaseOrderItemCreateRequest(
                20L,
                3,
                new BigDecimal("100.00")
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void cannotOrderWithoutItems() {
        PurchaseOrder order = purchaseOrder();
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> purchaseOrderService.updateStatus(
                10L,
                new PurchaseOrderStatusUpdateRequest(PurchaseOrderStatus.ORDERED)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    void canMarkPurchaseOrderAsOrderedWhenItHasItems() {
        PurchaseOrder order = purchaseOrder();
        order.addItem(PurchaseOrderItem.builder()
                .product(product())
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .lineTotal(new BigDecimal("200.00"))
                .build());

        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        PurchaseOrderResponse response = purchaseOrderService.updateStatus(
                10L,
                new PurchaseOrderStatusUpdateRequest(PurchaseOrderStatus.ORDERED)
        );

        assertThat(response.status()).isEqualTo(PurchaseOrderStatus.ORDERED);
    }

    @Test
    void cannotUseStatusUpdateToMarkOrderAsReceived() {
        PurchaseOrder order = purchaseOrder();
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> purchaseOrderService.updateStatus(
                10L,
                new PurchaseOrderStatusUpdateRequest(PurchaseOrderStatus.RECEIVED)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("receive workflow");
    }

    @Test
    void receivingPurchaseOrderIncreasesStockAndMarksOrderAsReceived() {
        Product product = product();
        PurchaseOrder order = orderedPurchaseOrder(product, 4);
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(inventoryMovementService.recordMovement(
                same(product),
                eq(MovementType.IN),
                eq(4),
                eq("Purchase order received: 10")
        )).thenAnswer(invocation -> {
            product.setQuantity(product.getQuantity() + 4);
            return null;
        });

        PurchaseOrderResponse response = purchaseOrderService.receive(10L);

        assertThat(response.status()).isEqualTo(PurchaseOrderStatus.RECEIVED);
        assertThat(product.getQuantity()).isEqualTo(14);
        verify(inventoryMovementService).recordMovement(
                same(product),
                eq(MovementType.IN),
                eq(4),
                eq("Purchase order received: 10")
        );
        verify(ledgerService).recordExpense(
                eq(new BigDecimal("400.00")),
                eq("Purchase order received: 10"),
                eq(LedgerSourceType.PURCHASE_ORDER),
                eq(10L)
        );
        verify(auditLogService).record(
                AuditAction.PURCHASE_ORDER_RECEIVED,
                "PurchaseOrder",
                10L,
                "Purchase order received and posted to inventory and ledger"
        );
    }

    @Test
    void cannotReceivePurchaseOrderTwice() {
        PurchaseOrder order = orderedPurchaseOrder(product(), 4);
        order.setStatus(PurchaseOrderStatus.RECEIVED);
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> purchaseOrderService.receive(10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already been received");

        verify(inventoryMovementService, never()).recordMovement(any(), any(), any(), any());
        verify(ledgerService, never()).recordExpense(any(), any(), any(), any());
        verify(auditLogService, never()).record(any(), any(), any(), any());
    }

    @Test
    void cannotReceiveCancelledPurchaseOrder() {
        PurchaseOrder order = orderedPurchaseOrder(product(), 4);
        order.setStatus(PurchaseOrderStatus.CANCELLED);
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> purchaseOrderService.receive(10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Cancelled");

        verify(inventoryMovementService, never()).recordMovement(any(), any(), any(), any());
        verify(ledgerService, never()).recordExpense(any(), any(), any(), any());
        verify(auditLogService, never()).record(any(), any(), any(), any());
    }

    @Test
    void cannotReceiveDraftPurchaseOrder() {
        PurchaseOrder order = purchaseOrder();
        when(purchaseOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> purchaseOrderService.receive(10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ORDERED");

        verify(inventoryMovementService, never()).recordMovement(any(), any(), any(), any());
        verify(ledgerService, never()).recordExpense(any(), any(), any(), any());
        verify(auditLogService, never()).record(any(), any(), any(), any());
    }

    private PurchaseOrder purchaseOrder() {
        return PurchaseOrder.builder()
                .id(10L)
                .supplier(supplier())
                .status(PurchaseOrderStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }

    private PurchaseOrder orderedPurchaseOrder(Product product, int quantity) {
        PurchaseOrder order = purchaseOrder();
        order.addItem(PurchaseOrderItem.builder()
                .product(product)
                .quantity(quantity)
                .unitPrice(new BigDecimal("100.00"))
                .lineTotal(BigDecimal.valueOf(quantity).multiply(new BigDecimal("100.00")))
                .build());
        order.setStatus(PurchaseOrderStatus.ORDERED);
        return order;
    }

    private Supplier supplier() {
        return Supplier.builder()
                .id(1L)
                .name("Nordic Supplies AS")
                .email("orders@nordic.example")
                .build();
    }

    private Product product() {
        return Product.builder()
                .id(20L)
                .name("Barcode Scanner")
                .sku("SCAN-001")
                .category("Hardware")
                .quantity(10)
                .minimumStock(3)
                .price(new BigDecimal("799.00"))
                .build();
    }
}
