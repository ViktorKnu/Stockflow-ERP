package com.stockflow.product;

import com.stockflow.audit.AuditAction;
import com.stockflow.audit.AuditLogService;
import com.stockflow.exception.DuplicateResourceException;
import com.stockflow.exception.ResourceNotFoundException;
import com.stockflow.product.dto.ProductCreateRequest;
import com.stockflow.product.dto.ProductResponse;
import com.stockflow.supplier.Supplier;
import com.stockflow.supplier.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ProductService productService;

    @Test
    void canCreateProduct() {
        Supplier supplier = supplier();
        ProductCreateRequest request = new ProductCreateRequest(
                "Barcode Scanner",
                "SCAN-001",
                "USB scanner",
                "Hardware",
                12,
                3,
                new BigDecimal("799.00"),
                supplier.getId()
        );

        when(productRepository.existsBySku("SCAN-001")).thenReturn(false);
        when(supplierRepository.findById(supplier.getId())).thenReturn(Optional.of(supplier));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(10L);
            return product;
        });

        ProductResponse response = productService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.sku()).isEqualTo("SCAN-001");
        assertThat(response.supplier().id()).isEqualTo(supplier.getId());
        assertThat(response.lowStock()).isFalse();
        verify(auditLogService).record(
                AuditAction.PRODUCT_CREATED,
                "Product",
                10L,
                "Product created with SKU SCAN-001"
        );
    }

    @Test
    void cannotCreateProductWithDuplicateSku() {
        ProductCreateRequest request = new ProductCreateRequest(
                "Barcode Scanner",
                "SCAN-001",
                null,
                "Hardware",
                12,
                3,
                new BigDecimal("799.00"),
                null
        );

        when(productRepository.existsBySku("SCAN-001")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("SCAN-001");

        verify(productRepository, never()).save(any(Product.class));
        verify(auditLogService, never()).record(any(), any(), any(), any());
    }

    @Test
    void canFindLowStockProducts() {
        Product lowStockProduct = product("BOX-001", 2, 5);
        when(productRepository.findLowStockProducts()).thenReturn(List.of(lowStockProduct));

        List<ProductResponse> lowStockProducts = productService.findLowStock();

        assertThat(lowStockProducts).hasSize(1);
        assertThat(lowStockProducts.getFirst().sku()).isEqualTo("BOX-001");
        assertThat(lowStockProducts.getFirst().lowStock()).isTrue();
    }

    @Test
    void throwsWhenSupplierDoesNotExistDuringCreate() {
        ProductCreateRequest request = new ProductCreateRequest(
                "Receipt Printer",
                "PRN-001",
                null,
                "Hardware",
                4,
                1,
                new BigDecimal("1499.00"),
                99L
        );

        when(productRepository.existsBySku("PRN-001")).thenReturn(false);
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Supplier not found");
    }

    private Product product(String sku, int quantity, int minimumStock) {
        return Product.builder()
                .id(1L)
                .name("Storage Box")
                .sku(sku)
                .description("Reusable storage box")
                .category("Storage")
                .quantity(quantity)
                .minimumStock(minimumStock)
                .price(new BigDecimal("49.00"))
                .build();
    }

    private Supplier supplier() {
        return Supplier.builder()
                .id(1L)
                .name("Nordic Supplies AS")
                .email("orders@nordic.example")
                .build();
    }
}
