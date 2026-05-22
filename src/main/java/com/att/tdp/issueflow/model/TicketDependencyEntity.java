package com.att.tdp.issueflow.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ticket_dependencies", uniqueConstraints = @UniqueConstraint(columnNames = {"ticket_id", "blocked_by_id"}))
@Getter
@Setter
@NoArgsConstructor
public class TicketDependencyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private TicketEntity ticket;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_by_id")
    private TicketEntity blockedBy;
}
