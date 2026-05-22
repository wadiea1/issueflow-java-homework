package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.model.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    List<ProjectEntity> findByDeletedFalseOrderByIdAsc();
    List<ProjectEntity> findByDeletedTrueOrderByIdAsc();
}
