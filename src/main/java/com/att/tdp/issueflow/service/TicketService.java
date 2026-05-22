package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.*;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.exception.NotFoundException;
import com.att.tdp.issueflow.model.*;
import com.att.tdp.issueflow.repository.TicketDependencyRepository;
import com.att.tdp.issueflow.repository.TicketRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketDependencyRepository dependencyRepository;
    private final ProjectService projectService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public TicketService(TicketRepository ticketRepository, TicketDependencyRepository dependencyRepository, ProjectService projectService, UserService userService, UserRepository userRepository, AuditLogService auditLogService) {
        this.ticketRepository = ticketRepository;
        this.dependencyRepository = dependencyRepository;
        this.projectService = projectService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public List<TicketResponse> listByProject(Long projectId) {
        projectService.find(projectId);
        return ticketRepository.findByProject_IdAndDeletedFalseOrderByIdAsc(projectId).stream().map(TicketResponse::from).toList();
    }

    public List<TicketResponse> deletedByProject(Long projectId) {
        projectService.find(projectId);
        return ticketRepository.findByProject_IdAndDeletedTrueOrderByIdAsc(projectId).stream().map(TicketResponse::from).toList();
    }

    public TicketResponse get(Long id) {
        TicketEntity ticket = find(id);
        if (ticket.isDeleted()) {
            throw new NotFoundException("Ticket is deleted: " + id);
        }
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse create(TicketRequest request) {
        ProjectEntity project = projectService.find(request.projectId());
        if (project.isDeleted()) {
            throw new BadRequestException("Cannot create ticket in a deleted project");
        }
        TicketEntity ticket = new TicketEntity();
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description());
        ticket.setStatus(request.status() == null ? TicketStatus.TODO : request.status());
        ticket.setPriority(request.priority() == null ? Priority.MEDIUM : request.priority());
        ticket.setType(request.type());
        ticket.setProject(project);
        ticket.setDueDate(request.dueDate());

        if (request.assigneeId() != null) {
            ticket.setAssignee(userService.find(request.assigneeId()));
        } else {
            UserEntity assignee = chooseLeastLoadedDeveloper(project.getId());
            ticket.setAssignee(assignee);
        }

        TicketEntity saved = ticketRepository.save(ticket);
        auditLogService.userAction(AuditAction.CREATE, "TICKET", saved.getId(), "Created ticket");
        if (request.assigneeId() == null && saved.getAssignee() != null) {
            auditLogService.systemAction(AuditAction.AUTO_ASSIGN, "TICKET", saved.getId(), "Auto-assigned to " + saved.getAssignee().getUsername());
        }
        return TicketResponse.from(saved);
    }

    @Transactional
    public void update(Long id, TicketUpdateRequest request) {
        TicketEntity ticket = find(id);
        if (ticket.isDeleted()) {
            throw new NotFoundException("Ticket is deleted: " + id);
        }
        if (ticket.getStatus() == TicketStatus.DONE) {
            throw new BadRequestException("A DONE ticket cannot be updated");
        }

        if (request.title() != null && !request.title().isBlank()) {
            ticket.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            ticket.setDescription(request.description());
        }
        if (request.status() != null) {
            validateStatusMove(ticket, request.status());
            if (request.status() == TicketStatus.DONE && dependencyRepository.existsByTicket_IdAndBlockedBy_StatusNot(id, TicketStatus.DONE)) {
                throw new BadRequestException("Ticket cannot move to DONE while it has unresolved blockers");
            }
            ticket.setStatus(request.status());
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
            ticket.setOverdue(false);
        }
        if (request.assigneeId() != null) {
            ticket.setAssignee(userService.find(request.assigneeId()));
        }
        if (request.dueDate() != null) {
            ticket.setDueDate(request.dueDate());
        }
        auditLogService.userAction(AuditAction.UPDATE, "TICKET", id, "Updated ticket");
    }

    @Transactional
    public void softDelete(Long id) {
        TicketEntity ticket = find(id);
        ticket.setDeleted(true);
        auditLogService.userAction(AuditAction.DELETE, "TICKET", id, "Soft-deleted ticket");
    }

    @Transactional
    public void restore(Long id) {
        TicketEntity ticket = find(id);
        ticket.setDeleted(false);
        auditLogService.userAction(AuditAction.RESTORE, "TICKET", id, "Restored ticket");
    }

    public String exportCsv(Long projectId) {
        List<TicketEntity> tickets = ticketRepository.findByProject_IdAndDeletedFalseOrderByIdAsc(projectId);
        try (StringWriter writer = new StringWriter(); CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("id", "title", "description", "status", "priority", "type", "assigneeId"))) {
            for (TicketEntity ticket : tickets) {
                printer.printRecord(
                        ticket.getId(),
                        ticket.getTitle(),
                        ticket.getDescription(),
                        ticket.getStatus(),
                        ticket.getPriority(),
                        ticket.getType(),
                        ticket.getAssignee() == null ? "" : ticket.getAssignee().getId()
                );
            }
            auditLogService.userAction(AuditAction.EXPORT, "TICKET", null, "Exported tickets for project " + projectId);
            return writer.toString();
        } catch (Exception e) {
            throw new BadRequestException("Could not export CSV: " + e.getMessage());
        }
    }

    @Transactional
    public ImportSummaryResponse importCsv(Long projectId, MultipartFile file) {
        ProjectEntity project = projectService.find(projectId);
        int created = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
            for (CSVRecord record : parser) {
                try {
                    TicketRequest request = new TicketRequest(
                            required(record, "title"),
                            value(record, "description"),
                            enumValue(record, "status", TicketStatus.class, TicketStatus.TODO),
                            enumValue(record, "priority", Priority.class, Priority.MEDIUM),
                            enumValue(record, "type", TicketType.class, TicketType.BUG),
                            project.getId(),
                            longValue(record, "assigneeId"),
                            null
                    );
                    create(request);
                    created++;
                } catch (Exception e) {
                    failed++;
                    errors.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
            auditLogService.userAction(AuditAction.IMPORT, "TICKET", null, "Imported tickets for project " + projectId);
            return new ImportSummaryResponse(created, failed, errors);
        } catch (Exception e) {
            throw new BadRequestException("Could not import CSV: " + e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${issueflow.escalation.fixed-delay-ms:60000}")
    @Transactional
    public void escalateOverdueTickets() {
        List<TicketEntity> tickets = ticketRepository.findByDeletedFalseAndStatusNotAndDueDateBefore(TicketStatus.DONE, Instant.now());
        for (TicketEntity ticket : tickets) {
            if (ticket.getPriority() == Priority.CRITICAL) {
                if (!ticket.isOverdue()) {
                    ticket.setOverdue(true);
                    auditLogService.systemAction(AuditAction.AUTO_ESCALATE, "TICKET", ticket.getId(), "Marked CRITICAL ticket as overdue");
                }
            } else {
                ticket.setPriority(ticket.getPriority().next());
                ticket.setOverdue(false);
                auditLogService.systemAction(AuditAction.AUTO_ESCALATE, "TICKET", ticket.getId(), "Escalated priority to " + ticket.getPriority());
            }
        }
    }

    public TicketEntity find(Long id) {
        return ticketRepository.findById(id).orElseThrow(() -> new NotFoundException("Ticket not found: " + id));
    }

    private UserEntity chooseLeastLoadedDeveloper(Long projectId) {
        return userRepository.findByRoleOrderByIdAsc(Role.DEVELOPER).stream()
                .min(Comparator.comparingLong((UserEntity user) -> ticketRepository.countByProject_IdAndAssignee_IdAndDeletedFalseAndStatusNot(projectId, user.getId(), TicketStatus.DONE))
                        .thenComparing(UserEntity::getId))
                .orElse(null);
    }

    private void validateStatusMove(TicketEntity ticket, TicketStatus newStatus) {
        if (newStatus.ordinal() < ticket.getStatus().ordinal()) {
            throw new BadRequestException("Status cannot move backward");
        }
    }

    private String required(CSVRecord record, String key) {
        String value = value(record, key);
        if (value == null || value.isBlank()) {
            throw new BadRequestException("Missing required field " + key);
        }
        return value;
    }

    private String value(CSVRecord record, String key) {
        return record.isMapped(key) ? record.get(key) : null;
    }

    private Long longValue(CSVRecord record, String key) {
        String value = value(record, key);
        return value == null || value.isBlank() ? null : Long.parseLong(value.trim());
    }

    private <E extends Enum<E>> E enumValue(CSVRecord record, String key, Class<E> type, E fallback) {
        String value = value(record, key);
        return value == null || value.isBlank() ? fallback : Enum.valueOf(type, value.trim());
    }
}
