package com.stockflow.audit;

import com.stockflow.audit.dto.AuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "List audit logs")
    public List<AuditLogResponse> findAll() {
        return auditLogService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log by id")
    public AuditLogResponse findById(@PathVariable Long id) {
        return auditLogService.findById(id);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "List audit logs for an entity")
    public List<AuditLogResponse> findByEntity(@PathVariable String entityType, @PathVariable Long entityId) {
        return auditLogService.findByEntity(entityType, entityId);
    }
}
