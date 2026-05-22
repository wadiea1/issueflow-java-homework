package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.model.Priority;
import com.att.tdp.issueflow.model.TicketEntity;
import com.att.tdp.issueflow.model.TicketStatus;
import com.att.tdp.issueflow.model.TicketType;

import java.time.Instant;

public record TicketResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        Priority priority,
        TicketType type,
        Long projectId,
        Long assigneeId,
        Instant dueDate,
        Boolean isOverdue
) {
    public static TicketResponse from(TicketEntity ticket) {
        Long assigneeId = ticket.getAssignee() == null ? null : ticket.getAssignee().getId();
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getType(),
                ticket.getProject().getId(),
                assigneeId,
                ticket.getDueDate(),
                ticket.isOverdue()
        );
    }
}
