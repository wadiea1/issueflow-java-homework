package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.model.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
}
