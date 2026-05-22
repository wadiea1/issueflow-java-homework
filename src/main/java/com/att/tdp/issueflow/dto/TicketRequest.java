package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.model.Priority;
import com.att.tdp.issueflow.model.TicketStatus;
import com.att.tdp.issueflow.model.TicketType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TicketRequest(
        @NotBlank String title,
        String description,
        TicketStatus status,
        Priority priority,
        @NotNull TicketType type,
        @NotNull Long projectId,
        Long assigneeId,
        Instant dueDate
) {}
