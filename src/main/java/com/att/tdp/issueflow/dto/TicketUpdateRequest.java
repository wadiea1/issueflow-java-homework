package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.model.Priority;
import com.att.tdp.issueflow.model.TicketStatus;

import java.time.Instant;

public record TicketUpdateRequest(
        String title,
        String description,
        TicketStatus status,
        Priority priority,
        Long assigneeId,
        Instant dueDate
) {}
