package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.AuditLogResponse;
import com.att.tdp.issueflow.model.*;
import com.att.tdp.issueflow.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final CurrentUserService currentUserService;

    public AuditLogService(AuditLogRepository auditLogRepository, CurrentUserService currentUserService) {
        this.auditLogRepository = auditLogRepository;
        this.currentUserService = currentUserService;
    }

    public void userAction(AuditAction action, String entityType, Long entityId, String details) {
        save(action, entityType, entityId, currentUserService.currentUserIdOrNull(), AuditActor.USER, details);
    }

    public void systemAction(AuditAction action, String entityType, Long entityId, String details) {
        save(action, entityType, entityId, null, AuditActor.SYSTEM, details);
    }

    public List<AuditLogResponse> list(String entityType, Long entityId, AuditAction action, AuditActor actor) {
        return auditLogRepository.findAll().stream()
                .filter(l -> entityType == null || l.getEntityType().equalsIgnoreCase(entityType))
                .filter(l -> entityId == null || Objects.equals(l.getEntityId(), entityId))
                .filter(l -> action == null || l.getAction() == action)
                .filter(l -> actor == null || l.getActor() == actor)
                .map(AuditLogResponse::from)
                .toList();
    }

    private void save(AuditAction action, String entityType, Long entityId, Long performedBy, AuditActor actor, String details) {
        AuditLogEntity log = new AuditLogEntity();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setPerformedBy(performedBy);
        log.setActor(actor);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
