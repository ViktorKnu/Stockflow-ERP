package com.stockflow.audit;

import com.stockflow.audit.dto.AuditLogResponse;
import com.stockflow.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @Test
    void canRecordAuditLog() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog auditLog = invocation.getArgument(0);
            auditLog.setId(1L);
            auditLog.setCreatedAt(LocalDateTime.of(2026, 6, 12, 12, 0));
            return auditLog;
        });

        AuditLogResponse response = auditLogService.record(
                AuditAction.PURCHASE_ORDER_RECEIVED,
                "PurchaseOrder",
                10L,
                "Purchase order received"
        );

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.actor()).isEqualTo("system");
        assertThat(response.action()).isEqualTo(AuditAction.PURCHASE_ORDER_RECEIVED);
        assertThat(response.entityType()).isEqualTo("PurchaseOrder");
        assertThat(response.entityId()).isEqualTo(10L);
    }

    @Test
    void canFindAuditLogsForEntity() {
        AuditLog auditLog = auditLog();
        when(auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc("Product", 5L))
                .thenReturn(List.of(auditLog));

        List<AuditLogResponse> response = auditLogService.findByEntity("Product", 5L);

        assertThat(response).hasSize(1);
        assertThat(response.getFirst().entityType()).isEqualTo("Product");
        assertThat(response.getFirst().entityId()).isEqualTo(5L);
    }

    @Test
    void throwsWhenAuditLogDoesNotExist() {
        when(auditLogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditLogService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Audit log not found");
    }

    private AuditLog auditLog() {
        return AuditLog.builder()
                .id(2L)
                .actor("system")
                .action(AuditAction.PRODUCT_UPDATED)
                .entityType("Product")
                .entityId(5L)
                .description("Product updated")
                .createdAt(LocalDateTime.of(2026, 6, 12, 12, 0))
                .build();
    }
}
