package com.stockflow.audit;

import com.stockflow.audit.dto.AuditLogResponse;
import com.stockflow.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Validated
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    @Operation(summary = "List audit logs")
    public PageResponse<AuditLogResponse> findAll(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return auditLogService.findAll(page, size);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit log by id")
    public AuditLogResponse findById(@PathVariable Long id) {
        return auditLogService.findById(id);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "List audit logs for an entity")
    public PageResponse<AuditLogResponse> findByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        return auditLogService.findByEntity(entityType, entityId, page, size);
    }
}
