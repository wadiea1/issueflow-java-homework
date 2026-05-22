package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.model.AttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<AttachmentEntity, Long> {
    Optional<AttachmentEntity> findByIdAndTicket_Id(Long id, Long ticketId);
}
