package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.DependencyResponse;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.exception.NotFoundException;
import com.att.tdp.issueflow.model.AuditAction;
import com.att.tdp.issueflow.model.TicketDependencyEntity;
import com.att.tdp.issueflow.model.TicketEntity;
import com.att.tdp.issueflow.repository.TicketDependencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DependencyService {
    private final TicketDependencyRepository dependencyRepository;
    private final TicketService ticketService;
    private final AuditLogService auditLogService;

    public DependencyService(TicketDependencyRepository dependencyRepository, TicketService ticketService, AuditLogService auditLogService) {
        this.dependencyRepository = dependencyRepository;
        this.ticketService = ticketService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public void add(Long ticketId, Long blockerId) {
        if (ticketId.equals(blockerId)) {
            throw new BadRequestException("A ticket cannot depend on itself");
        }
        TicketEntity ticket = ticketService.find(ticketId);
        TicketEntity blocker = ticketService.find(blockerId);
        if (!ticket.getProject().getId().equals(blocker.getProject().getId())) {
            throw new BadRequestException("Both tickets must belong to the same project");
        }
        dependencyRepository.findByTicket_IdAndBlockedBy_Id(ticketId, blockerId).ifPresent(existing -> {
            throw new BadRequestException("Dependency already exists");
        });
        TicketDependencyEntity dependency = new TicketDependencyEntity();
        dependency.setTicket(ticket);
        dependency.setBlockedBy(blocker);
        dependencyRepository.save(dependency);
        auditLogService.userAction(AuditAction.ADD_DEPENDENCY, "TICKET", ticketId, "Blocked by ticket " + blockerId);
    }

    public List<DependencyResponse> list(Long ticketId) {
        ticketService.find(ticketId);
        return dependencyRepository.findByTicket_IdOrderByBlockedBy_IdAsc(ticketId).stream()
                .map(dep -> DependencyResponse.from(dep.getBlockedBy()))
                .toList();
    }

    @Transactional
    public void remove(Long ticketId, Long blockerId) {
        TicketDependencyEntity dependency = dependencyRepository.findByTicket_IdAndBlockedBy_Id(ticketId, blockerId)
                .orElseThrow(() -> new NotFoundException("Dependency not found"));
        dependencyRepository.delete(dependency);
        auditLogService.userAction(AuditAction.REMOVE_DEPENDENCY, "TICKET", ticketId, "Removed blocker ticket " + blockerId);
    }
}
