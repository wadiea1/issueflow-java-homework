package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.model.TicketEntity;
import com.att.tdp.issueflow.model.TicketStatus;

public record DependencyResponse(Long id, String title, TicketStatus status) {
    public static DependencyResponse from(TicketEntity ticket) {
        return new DependencyResponse(ticket.getId(), ticket.getTitle(), ticket.getStatus());
    }
}
