package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.model.TicketDependencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketDependencyRepository extends JpaRepository<TicketDependencyEntity, Long> {
    List<TicketDependencyEntity> findByTicket_IdOrderByBlockedBy_IdAsc(Long ticketId);
    Optional<TicketDependencyEntity> findByTicket_IdAndBlockedBy_Id(Long ticketId, Long blockedById);
    boolean existsByTicket_IdAndBlockedBy_StatusNot(Long ticketId, com.att.tdp.issueflow.model.TicketStatus status);
}
