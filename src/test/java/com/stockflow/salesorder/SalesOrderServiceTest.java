package com.stockflow.salesorder;

import com.stockflow.exception.BusinessRuleException;
import com.stockflow.product.Product;
import com.stockflow.product.ProductRepository;
import com.stockflow.salesorder.dto.SalesOrderCreateRequest;
import com.stockflow.salesorder.dto.SalesOrderItemCreateRequest;
import com.stockflow.salesorder.dto.SalesOrderResponse;
import com.stockflow.salesorder.dto.SalesOrderStatusUpdateRequest;
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
class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SalesOrderService salesOrderService;

    @Test
    void canCreateSalesOrder() {
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> {
            SalesOrder order = invocation.getArgument(0);
            order.setId(10L);
            return order;
        });

        SalesOrderResponse response = salesOrderService.create(new SalesOrderCreateRequest(
                "Ada Lovelace",
                "ada@example.com"
        ));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.customerName()).isEqualTo("Ada Lovelace");
        assertThat(response.customerEmail()).isEqualTo("ada@example.com");
        assertThat(response.status()).isEqualTo(SalesOrderStatus.DRAFT);
        assertThat(response.totalAmount()).isEqualByComparingTo("0.00");
        assertThat(response.items()).isEmpty();
    }

    @Test
    void canAddItemAndRecalculateTotalAmount() {
        SalesOrder order = salesOrder();
        Product product = product(10);
        when(salesOrderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(productRepository.findById(20L)).thenReturn(Optional.of(product));

        SalesOrderResponse response = salesOrderService.addItem(10L, new SalesOrderItemCreateRequest(
                20L,
                3,
                new BigDecimal("150.00")
        ));

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().productId()).isEqualTo(20L);
        assertThat(response.items().getFirst().lineTotal()).isEqualByComparingTo("450.00");
        assertThat(response.totalAmount()).isEqualByComparingTo("450.00");
    }

    @Test
    void cannotAddItemWhenOrderIsNotDraft() {
        SalesOrder order = salesOrder();
        order.setStatus(SalesOrderStatus.CONFIRMED);
        when(salesOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> salesOrderService.addItem(10L, new SalesOrderItemCreateRequest(
                20L,
                3,
                new BigDecimal("150.00")
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test
    void canConfirmSalesOrderWhenStockIsAvailable() {
        SalesOrder order = salesOrderWithItem(product(10), 3);
        when(salesOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        SalesOrderResponse response = salesOrderService.updateStatus(
                10L,
                new SalesOrderStatusUpdateRequest(SalesOrderStatus.CONFIRMED)
        );

        assertThat(response.status()).isEqualTo(SalesOrderStatus.CONFIRMED);
    }

    @Test
    void cannotConfirmSalesOrderWithoutItems() {
        SalesOrder order = salesOrder();
        when(salesOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> salesOrderService.updateStatus(
                10L,
                new SalesOrderStatusUpdateRequest(SalesOrderStatus.CONFIRMED)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    void cannotConfirmSalesOrderWithoutEnoughStock() {
        SalesOrder order = salesOrderWithItem(product(2), 3);
        when(salesOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> salesOrderService.updateStatus(
                10L,
                new SalesOrderStatusUpdateRequest(SalesOrderStatus.CONFIRMED)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Not enough stock");
    }

    @Test
    void canMarkConfirmedSalesOrderAsPaid() {
        SalesOrder order = salesOrderWithItem(product(10), 3);
        order.setStatus(SalesOrderStatus.CONFIRMED);
        when(salesOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        SalesOrderResponse response = salesOrderService.updateStatus(
                10L,
                new SalesOrderStatusUpdateRequest(SalesOrderStatus.PAID)
        );

        assertThat(response.status()).isEqualTo(SalesOrderStatus.PAID);
    }

    @Test
    void cannotMarkDraftSalesOrderAsPaid() {
        SalesOrder order = salesOrderWithItem(product(10), 3);
        when(salesOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> salesOrderService.updateStatus(
                10L,
                new SalesOrderStatusUpdateRequest(SalesOrderStatus.PAID)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("CONFIRMED");
    }

    @Test
    void cannotUseStatusUpdateToMarkOrderAsShipped() {
        SalesOrder order = salesOrderWithItem(product(10), 3);
        order.setStatus(SalesOrderStatus.PAID);
        when(salesOrderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> salesOrderService.updateStatus(
                10L,
                new SalesOrderStatusUpdateRequest(SalesOrderStatus.SHIPPED)
        ))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ship workflow");
    }

    private SalesOrder salesOrder() {
        return SalesOrder.builder()
                .id(10L)
                .customerName("Ada Lovelace")
                .customerEmail("ada@example.com")
                .status(SalesOrderStatus.DRAFT)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();
    }

    private SalesOrder salesOrderWithItem(Product product, int quantity) {
        SalesOrder order = salesOrder();
        order.addItem(SalesOrderItem.builder()
                .product(product)
                .quantity(quantity)
                .unitPrice(new BigDecimal("150.00"))
                .lineTotal(BigDecimal.valueOf(quantity).multiply(new BigDecimal("150.00")))
                .build());
        return order;
    }

    private Product product(int quantity) {
        return Product.builder()
                .id(20L)
                .name("Barcode Scanner")
                .sku("SCAN-001")
                .category("Hardware")
                .quantity(quantity)
                .minimumStock(3)
                .price(new BigDecimal("799.00"))
                .build();
    }
}
