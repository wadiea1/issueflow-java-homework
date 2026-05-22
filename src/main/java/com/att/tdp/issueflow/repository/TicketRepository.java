package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.model.TicketEntity;
import com.att.tdp.issueflow.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
    List<TicketEntity> findByProject_IdAndDeletedFalseOrderByIdAsc(Long projectId);
    List<TicketEntity> findByProject_IdAndDeletedTrueOrderByIdAsc(Long projectId);
    long countByProject_IdAndAssignee_IdAndDeletedFalseAndStatusNot(Long projectId, Long assigneeId, TicketStatus status);
    List<TicketEntity> findByDeletedFalseAndStatusNotAndDueDateBefore(TicketStatus status, Instant dueDate);
}
