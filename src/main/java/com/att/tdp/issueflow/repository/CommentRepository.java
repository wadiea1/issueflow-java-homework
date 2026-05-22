package com.att.tdp.issueflow.repository;

import com.att.tdp.issueflow.model.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByTicket_IdOrderByCreatedAtAsc(Long ticketId);
    List<CommentEntity> findByMentionedUsers_IdOrderByCreatedAtDesc(Long userId);
}
