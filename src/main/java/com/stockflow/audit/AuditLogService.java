package com.stockflow.audit;

import com.stockflow.audit.dto.AuditLogResponse;
import com.stockflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final String SYSTEM_ACTOR = "system";

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AuditLogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuditLogResponse findById(Long id) {
        return AuditLogMapper.toResponse(auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found: " + id)));
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId).stream()
                .map(AuditLogMapper::toResponse)
                .toList();
    }

    @Transactional
    public AuditLogResponse record(AuditAction action, String entityType, Long entityId, String description) {
        AuditLog auditLog = AuditLog.builder()
                .actor(SYSTEM_ACTOR)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .build();

        return AuditLogMapper.toResponse(auditLogRepository.save(auditLog));
    }
}
