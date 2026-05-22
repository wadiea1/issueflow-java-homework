package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.*;
import com.att.tdp.issueflow.exception.NotFoundException;
import com.att.tdp.issueflow.model.*;
import com.att.tdp.issueflow.repository.ProjectRepository;
import com.att.tdp.issueflow.repository.TicketRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final AuditLogService auditLogService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, TicketRepository ticketRepository, UserService userService, AuditLogService auditLogService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.auditLogService = auditLogService;
    }

    public List<ProjectResponse> list() {
        return projectRepository.findByDeletedFalseOrderByIdAsc().stream().map(ProjectResponse::from).toList();
    }

    public List<ProjectResponse> deleted() {
        return projectRepository.findByDeletedTrueOrderByIdAsc().stream().map(ProjectResponse::from).toList();
    }

    public ProjectResponse get(Long id) {
        ProjectEntity project = find(id);
        if (project.isDeleted()) {
            throw new NotFoundException("Project is deleted: " + id);
        }
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        UserEntity owner = userService.find(request.ownerId());
        ProjectEntity project = new ProjectEntity();
        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setOwner(owner);
        ProjectEntity saved = projectRepository.save(project);
        auditLogService.userAction(AuditAction.CREATE, "PROJECT", saved.getId(), "Created project");
        return ProjectResponse.from(saved);
    }

    @Transactional
    public void update(Long id, ProjectUpdateRequest request) {
        ProjectEntity project = find(id);
        if (project.isDeleted()) {
            throw new NotFoundException("Project is deleted: " + id);
        }
        if (request.name() != null && !request.name().isBlank()) {
            project.setName(request.name().trim());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        auditLogService.userAction(AuditAction.UPDATE, "PROJECT", id, "Updated project");
    }

    @Transactional
    public void softDelete(Long id) {
        ProjectEntity project = find(id);
        project.setDeleted(true);
        auditLogService.userAction(AuditAction.DELETE, "PROJECT", id, "Soft-deleted project");
    }

    @Transactional
    public void restore(Long id) {
        ProjectEntity project = find(id);
        project.setDeleted(false);
        auditLogService.userAction(AuditAction.RESTORE, "PROJECT", id, "Restored project");
    }

    public List<WorkloadResponse> workload(Long projectId) {
        find(projectId);
        return userRepository.findByRoleOrderByIdAsc(Role.DEVELOPER).stream()
                .map(user -> new WorkloadResponse(
                        user.getId(),
                        user.getUsername(),
                        ticketRepository.countByProject_IdAndAssignee_IdAndDeletedFalseAndStatusNot(projectId, user.getId(), TicketStatus.DONE)
                ))
                .sorted(Comparator.comparingLong(WorkloadResponse::openTicketCount).thenComparing(WorkloadResponse::userId))
                .toList();
    }

    public ProjectEntity find(Long id) {
        return projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Project not found: " + id));
    }
}
