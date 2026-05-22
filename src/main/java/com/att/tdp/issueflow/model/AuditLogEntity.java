package com.att.tdp.issueflow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditAction action;

    @Column(nullable = false, length = 80)
    private String entityType;

    private Long entityId;

    private Long performedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AuditActor actor;

    @Column(length = 2000)
    private String details;

    @Column(nullable = false, updatable = false)
    private Instant timestamp = Instant.now();
}
