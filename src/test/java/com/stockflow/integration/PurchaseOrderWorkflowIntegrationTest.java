package com.stockflow.integration;

import com.jayway.jsonpath.JsonPath;
import com.stockflow.audit.AuditAction;
import com.stockflow.audit.AuditLogRepository;
import com.stockflow.inventory.InventoryMovementRepository;
import com.stockflow.inventory.MovementType;
import com.stockflow.ledger.LedgerSourceType;
import com.stockflow.ledger.LedgerTransactionRepository;
import com.stockflow.ledger.LedgerTransactionType;
import com.stockflow.product.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class PurchaseOrderWorkflowIntegrationTest {

    private static final String ADMIN_EMAIL = "integration-admin@stockflow.local";
    private static final String ADMIN_PASSWORD = "integration-password-123";

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("stockflow_integration")
            .withUsername("stockflow")
            .withPassword("stockflow");

    @DynamicPropertySource
    static void configureApplication(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("stockflow.security.jwt.secret",
                () -> "integration-test-jwt-secret-with-at-least-32-bytes");
        registry.add("stockflow.security.bootstrap-admin.name", () -> "Integration Admin");
        registry.add("stockflow.security.bootstrap-admin.email", () -> ADMIN_EMAIL);
        registry.add("stockflow.security.bootstrap-admin.password", () -> ADMIN_PASSWORD);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    private LedgerTransactionRepository ledgerTransactionRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void authenticatedPurchaseOrderWorkflowPersistsInventoryLedgerAndAuditData() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());

        String token = loginAsBootstrapAdmin();
        long supplierId = createSupplier(token);
        long productId = createProduct(token, supplierId);
        long purchaseOrderId = createPurchaseOrder(token, supplierId);

        mockMvc.perform(post("/api/purchase-orders/{id}/items", purchaseOrderId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 5,
                                  "unitPrice": 12.50
                                }
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(62.5));

        mockMvc.perform(put("/api/purchase-orders/{id}/status", purchaseOrderId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ORDERED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ORDERED"));

        mockMvc.perform(post("/api/purchase-orders/{id}/receive", purchaseOrderId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        assertThat(productRepository.findById(productId).orElseThrow().getQuantity()).isEqualTo(7);

        assertThat(inventoryMovementRepository.findByProductIdOrderByCreatedAtDesc(productId))
                .singleElement()
                .satisfies(movement -> {
                    assertThat(movement.getType()).isEqualTo(MovementType.IN);
                    assertThat(movement.getQuantity()).isEqualTo(5);
                    assertThat(movement.getPreviousQuantity()).isEqualTo(2);
                    assertThat(movement.getNewQuantity()).isEqualTo(7);
                });

        assertThat(ledgerTransactionRepository.findAllByOrderByCreatedAtDesc())
                .singleElement()
                .satisfies(transaction -> {
                    assertThat(transaction.getType()).isEqualTo(LedgerTransactionType.EXPENSE);
                    assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("62.50"));
                    assertThat(transaction.getSourceType()).isEqualTo(LedgerSourceType.PURCHASE_ORDER);
                    assertThat(transaction.getSourceId()).isEqualTo(purchaseOrderId);
                });

        assertThat(auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                "PurchaseOrder", purchaseOrderId))
                .singleElement()
                .satisfies(auditLog -> {
                    assertThat(auditLog.getAction()).isEqualTo(AuditAction.PURCHASE_ORDER_RECEIVED);
                    assertThat(auditLog.getActor()).isEqualTo(ADMIN_EMAIL);
                });

        mockMvc.perform(get("/api/ledger/transactions")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(62.5))
                .andExpect(jsonPath("$[0].sourceId").value(purchaseOrderId));
    }

    private String loginAsBootstrapAdmin() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonPath.read(response, "$.accessToken");
    }

    private long createSupplier(String token) throws Exception {
        String response = mockMvc.perform(post("/api/suppliers")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Integration Supplier",
                                  "email": "supplier@integration.test",
                                  "phone": "+47 12345678",
                                  "address": "Testveien 1"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private long createProduct(String token, long supplierId) throws Exception {
        String response = mockMvc.perform(post("/api/products")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Integration Product",
                                  "sku": "INT-001",
                                  "description": "Created by the integration test",
                                  "category": "Test",
                                  "quantity": 2,
                                  "minimumStock": 1,
                                  "price": 25.00,
                                  "supplierId": %d
                                }
                                """.formatted(supplierId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private long createPurchaseOrder(String token, long supplierId) throws Exception {
        String response = mockMvc.perform(post("/api/purchase-orders")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supplierId\":%d}".formatted(supplierId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
