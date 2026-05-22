package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.*;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.exception.NotFoundException;
import com.att.tdp.issueflow.model.AuditAction;
import com.att.tdp.issueflow.model.UserEntity;
import com.att.tdp.issueflow.repository.CommentRepository;
import com.att.tdp.issueflow.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository, CommentRepository commentRepository, PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public List<UserResponse> list() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse get(Long id) {
        return UserResponse.from(find(id));
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new BadRequestException("Username already exists");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Email already exists");
        }
        UserEntity user = new UserEntity();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim());
        user.setFullName(request.fullName().trim());
        user.setRole(request.role());
        String rawPassword = request.password() == null || request.password().isBlank() ? "secret" : request.password();
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        UserEntity saved = userRepository.save(user);
        auditLogService.userAction(AuditAction.CREATE, "USER", saved.getId(), "Created user " + saved.getUsername());
        return UserResponse.from(saved);
    }

    @Transactional
    public void update(Long id, UserUpdateRequest request) {
        UserEntity user = find(id);
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        auditLogService.userAction(AuditAction.UPDATE, "USER", id, "Updated user");
    }

    @Transactional
    public void delete(Long id) {
        UserEntity user = find(id);
        userRepository.delete(user);
        auditLogService.userAction(AuditAction.DELETE, "USER", id, "Deleted user");
    }

    public MentionPageResponse mentions(Long userId, int page, int pageSize) {
        find(userId);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 100);
        List<CommentResponse> all = commentRepository.findByMentionedUsers_IdOrderByCreatedAtDesc(userId).stream()
                .map(CommentResponse::from)
                .toList();
        int from = Math.min((safePage - 1) * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        return new MentionPageResponse(all.subList(from, to), all.size(), safePage);
    }

    public UserEntity find(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));
    }
}
