package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.model.ProjectEntity;

public record ProjectResponse(Long id, String name, String description, Long ownerId) {
    public static ProjectResponse from(ProjectEntity project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), project.getOwner().getId());
    }
}
