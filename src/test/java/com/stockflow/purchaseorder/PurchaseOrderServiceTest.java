package com.stockflow.purchaseorder;

import com.stockflow.exception.BusinessRuleException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ProductRepository productRepository;

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

    private PurchaseOrder purchaseOrder() {
        return PurchaseOrder.builder()
                .id(10L)
                .supplier(supplier())
                .status(PurchaseOrderStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
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
