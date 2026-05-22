package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.AuditLogResponse;
import com.att.tdp.issueflow.model.AuditAction;
import com.att.tdp.issueflow.model.AuditActor;
import com.att.tdp.issueflow.service.AuditLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogResponse> list(@RequestParam(required = false) String entityType,
                                       @RequestParam(required = false) Long entityId,
                                       @RequestParam(required = false) AuditAction action,
                                       @RequestParam(required = false) AuditActor actor) {
        return auditLogService.list(entityType, entityId, action, actor);
    }
}
