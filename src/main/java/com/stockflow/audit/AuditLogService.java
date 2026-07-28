package com.stockflow.audit;

import com.stockflow.audit.dto.AuditLogResponse;
import com.stockflow.common.dto.PageResponse;
import com.stockflow.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final String SYSTEM_ACTOR = "system";

    private final AuditLogRepository auditLogRepository;

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> findAll(int page, int size) {
        return PageResponse.from(
                auditLogRepository.findAll(pageRequest(page, size)),
                AuditLogMapper::toResponse
        );
    }

    @Transactional(readOnly = true)
    public AuditLogResponse findById(Long id) {
        return AuditLogMapper.toResponse(auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found: " + id)));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> findByEntity(
            String entityType, Long entityId, int page, int size) {
        return PageResponse.from(
                auditLogRepository.findByEntityTypeAndEntityId(
                        entityType, entityId, pageRequest(page, size)),
                AuditLogMapper::toResponse
        );
    }

    @Transactional
    public AuditLogResponse record(AuditAction action, String entityType, Long entityId, String description) {
        AuditLog auditLog = AuditLog.builder()
                .actor(currentActor())
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .build();

        return AuditLogMapper.toResponse(auditLogRepository.save(auditLog));
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return SYSTEM_ACTOR;
        }
        return authentication.getName();
    }

    private PageRequest pageRequest(int page, int size) {
        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );
    }
}
