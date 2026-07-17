package com.stockflow.integration;

import com.stockflow.inventory.InventoryMovementRepository;
import com.stockflow.product.ProductRepository;
import com.stockflow.purchaseorder.PurchaseOrderRepository;
import com.stockflow.purchaseorder.PurchaseOrderStatus;
import com.stockflow.salesorder.SalesOrderRepository;
import com.stockflow.salesorder.SalesOrderStatus;
import com.stockflow.supplier.SupplierRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("demo")
@Testcontainers(disabledWithoutDocker = true)
class DemoDataIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("stockflow_demo_test")
            .withUsername("stockflow")
            .withPassword("stockflow");

    @DynamicPropertySource
    static void configureApplication(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("stockflow.security.jwt.secret",
                () -> "demo-data-integration-secret-with-at-least-32-bytes");
    }

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Test
    void demoProfileLoadsAConsistentDataset() {
        assertThat(supplierRepository.findAll())
                .extracting("email")
                .containsExactlyInAnyOrder(
                        "orders@nordic-components.demo",
                        "sales@oslo-office.demo"
                );

        assertThat(productRepository.findAll())
                .hasSize(3)
                .extracting("sku")
                .containsExactlyInAnyOrder(
                        "DEMO-LAPTOP-001",
                        "DEMO-SCANNER-001",
                        "DEMO-LABEL-001"
                );

        assertThat(productRepository.findLowStockProducts())
                .extracting("sku")
                .containsExactly("DEMO-SCANNER-001");
        assertThat(inventoryMovementRepository.count()).isEqualTo(3);

        assertThat(purchaseOrderRepository.findAll())
                .singleElement()
                .satisfies(order -> {
                    assertThat(order.getStatus()).isEqualTo(PurchaseOrderStatus.ORDERED);
                    assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("12495.00"));
                });

        assertThat(salesOrderRepository.findAll())
                .singleElement()
                .satisfies(order -> {
                    assertThat(order.getStatus()).isEqualTo(SalesOrderStatus.CONFIRMED);
                    assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("25998.00"));
                });
    }
}
