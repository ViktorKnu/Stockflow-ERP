package com.stockflow.audit.dto;

import com.stockflow.audit.AuditAction;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        String actor,
        AuditAction action,
        String entityType,
        Long entityId,
        String description,
        LocalDateTime createdAt
) {
}
