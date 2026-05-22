package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.*;
import com.att.tdp.issueflow.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> list() {
        return projectService.list();
    }

    @GetMapping("/deleted")
    public List<ProjectResponse> deleted() {
        return projectService.deleted();
    }

    @GetMapping("/{projectId}")
    public ProjectResponse get(@PathVariable Long projectId) {
        return projectService.get(projectId);
    }

    @PostMapping
    public ProjectResponse create(@Valid @RequestBody ProjectRequest request) {
        return projectService.create(request);
    }

    @PatchMapping("/{projectId}")
    public void update(@PathVariable Long projectId, @RequestBody ProjectUpdateRequest request) {
        projectService.update(projectId, request);
    }

    @DeleteMapping("/{projectId}")
    public void delete(@PathVariable Long projectId) {
        projectService.softDelete(projectId);
    }

    @PostMapping("/{projectId}/restore")
    public void restore(@PathVariable Long projectId) {
        projectService.restore(projectId);
    }

    @GetMapping("/{projectId}/workload")
    public List<WorkloadResponse> workload(@PathVariable Long projectId) {
        return projectService.workload(projectId);
    }
}
