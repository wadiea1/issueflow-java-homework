package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.model.AuditAction;
import com.att.tdp.issueflow.model.AuditActor;
import com.att.tdp.issueflow.model.AuditLogEntity;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        AuditAction action,
        String entityType,
        Long entityId,
        Long performedBy,
        AuditActor actor,
        Instant timestamp,
        String details
) {
    public static AuditLogResponse from(AuditLogEntity log) {
        return new AuditLogResponse(log.getId(), log.getAction(), log.getEntityType(), log.getEntityId(), log.getPerformedBy(), log.getActor(), log.getTimestamp(), log.getDetails());
    }
}
