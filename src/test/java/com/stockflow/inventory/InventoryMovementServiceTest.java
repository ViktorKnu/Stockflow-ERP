package com.stockflow.inventory;

import com.stockflow.audit.AuditAction;
import com.stockflow.audit.AuditLogService;
import com.stockflow.exception.BusinessRuleException;
import com.stockflow.inventory.dto.InventoryMovementCreateRequest;
import com.stockflow.inventory.dto.InventoryMovementResponse;
import com.stockflow.product.Product;
import com.stockflow.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryMovementServiceTest {

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private InventoryMovementService movementService;

    @Test
    void inIncreasesQuantity() {
        Product product = product(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(movementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> {
            InventoryMovement movement = invocation.getArgument(0);
            movement.setId(100L);
            return movement;
        });

        InventoryMovementResponse response = movementService.create(new InventoryMovementCreateRequest(
                1L,
                MovementType.IN,
                5,
                "Supplier delivery"
        ));

        assertThat(product.getQuantity()).isEqualTo(15);
        assertThat(response.previousQuantity()).isEqualTo(10);
        assertThat(response.newQuantity()).isEqualTo(15);
        verify(auditLogService).record(
                AuditAction.INVENTORY_MOVEMENT_CREATED,
                "InventoryMovement",
                100L,
                "Inventory movement IN for product 1"
        );
    }

    @Test
    void outDecreasesQuantity() {
        Product product = product(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(movementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryMovementResponse response = movementService.create(new InventoryMovementCreateRequest(
                1L,
                MovementType.OUT,
                4,
                "Customer order shipped"
        ));

        assertThat(product.getQuantity()).isEqualTo(6);
        assertThat(response.previousQuantity()).isEqualTo(10);
        assertThat(response.newQuantity()).isEqualTo(6);
    }

    @Test
    void outCannotMakeStockNegative() {
        Product product = product(3);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> movementService.create(new InventoryMovementCreateRequest(
                1L,
                MovementType.OUT,
                4,
                "Customer order shipped"
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("negative");

        assertThat(product.getQuantity()).isEqualTo(3);
        verify(movementRepository, never()).save(any(InventoryMovement.class));
        verify(auditLogService, never()).record(any(), any(), any(), any());
    }

    @Test
    void adjustmentSetsNewQuantity() {
        Product product = product(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(movementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryMovementResponse response = movementService.create(new InventoryMovementCreateRequest(
                1L,
                MovementType.ADJUSTMENT,
                7,
                "Manual stock count"
        ));

        assertThat(product.getQuantity()).isEqualTo(7);
        assertThat(response.previousQuantity()).isEqualTo(10);
        assertThat(response.newQuantity()).isEqualTo(7);
    }

    @Test
    void movementStoresPreviousQuantityAndNewQuantity() {
        Product product = product(8);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(movementRepository.save(any(InventoryMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        movementService.create(new InventoryMovementCreateRequest(
                1L,
                MovementType.IN,
                2,
                "Restock"
        ));

        ArgumentCaptor<InventoryMovement> movementCaptor = ArgumentCaptor.forClass(InventoryMovement.class);
        verify(movementRepository).save(movementCaptor.capture());

        InventoryMovement movement = movementCaptor.getValue();
        assertThat(movement.getPreviousQuantity()).isEqualTo(8);
        assertThat(movement.getNewQuantity()).isEqualTo(10);
    }

    private Product product(int quantity) {
        return Product.builder()
                .id(1L)
                .name("Barcode Scanner")
                .sku("SCAN-001")
                .category("Hardware")
                .quantity(quantity)
                .minimumStock(3)
                .price(new BigDecimal("799.00"))
                .build();
    }
}
