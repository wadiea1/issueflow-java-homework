package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.CommentRequest;
import com.att.tdp.issueflow.dto.CommentResponse;
import com.att.tdp.issueflow.dto.CommentUpdateRequest;
import com.att.tdp.issueflow.exception.NotFoundException;
import com.att.tdp.issueflow.model.AuditAction;
import com.att.tdp.issueflow.model.CommentEntity;
import com.att.tdp.issueflow.model.TicketEntity;
import com.att.tdp.issueflow.model.UserEntity;
import com.att.tdp.issueflow.repository.CommentRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CommentService {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([A-Za-z0-9_.-]+)");

    private final CommentRepository commentRepository;
    private final TicketService ticketService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public CommentService(CommentRepository commentRepository, TicketService ticketService, UserService userService, UserRepository userRepository, AuditLogService auditLogService) {
        this.commentRepository = commentRepository;
        this.ticketService = ticketService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public List<CommentResponse> list(Long ticketId) {
        ticketService.find(ticketId);
        return commentRepository.findByTicket_IdOrderByCreatedAtAsc(ticketId).stream().map(CommentResponse::from).toList();
    }

    @Transactional
    public CommentResponse add(Long ticketId, CommentRequest request) {
        TicketEntity ticket = ticketService.find(ticketId);
        UserEntity author = userService.find(request.authorId());
        CommentEntity comment = new CommentEntity();
        comment.setTicket(ticket);
        comment.setAuthor(author);
        comment.setContent(request.content());
        comment.setMentionedUsers(resolveMentions(request.content()));
        CommentEntity saved = commentRepository.save(comment);
        auditLogService.userAction(AuditAction.CREATE, "COMMENT", saved.getId(), "Added comment");
        return CommentResponse.from(saved);
    }

    @Transactional
    public void update(Long ticketId, Long commentId, CommentUpdateRequest request) {
        ticketService.find(ticketId);
        CommentEntity comment = find(commentId);
        if (!comment.getTicket().getId().equals(ticketId)) {
            throw new NotFoundException("Comment does not belong to ticket");
        }
        comment.setContent(request.content());
        comment.setUpdatedAt(Instant.now());
        comment.setMentionedUsers(resolveMentions(request.content()));
        auditLogService.userAction(AuditAction.UPDATE, "COMMENT", commentId, "Updated comment");
    }

    @Transactional
    public void delete(Long ticketId, Long commentId) {
        ticketService.find(ticketId);
        CommentEntity comment = find(commentId);
        if (!comment.getTicket().getId().equals(ticketId)) {
            throw new NotFoundException("Comment does not belong to ticket");
        }
        commentRepository.delete(comment);
        auditLogService.userAction(AuditAction.DELETE, "COMMENT", commentId, "Deleted comment");
    }

    private CommentEntity find(Long id) {
        return commentRepository.findById(id).orElseThrow(() -> new NotFoundException("Comment not found: " + id));
    }

    private Set<UserEntity> resolveMentions(String content) {
        Set<UserEntity> users = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content == null ? "" : content);
        while (matcher.find()) {
            String username = matcher.group(1);
            userRepository.findByUsernameIgnoreCase(username).ifPresent(users::add);
        }
        return users;
    }
}
