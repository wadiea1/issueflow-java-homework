package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.AuthLoginRequest;
import com.att.tdp.issueflow.dto.AuthTokenResponse;
import com.att.tdp.issueflow.dto.UserResponse;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.model.AuditAction;
import com.att.tdp.issueflow.model.UserEntity;
import com.att.tdp.issueflow.repository.UserRepository;
import com.att.tdp.issueflow.security.JwtService;
import com.att.tdp.issueflow.security.TokenBlacklist;
import com.att.tdp.issueflow.service.AuditLogService;
import com.att.tdp.issueflow.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklist tokenBlacklist;
    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, TokenBlacklist tokenBlacklist, CurrentUserService currentUserService, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenBlacklist = tokenBlacklist;
        this.currentUserService = currentUserService;
        this.auditLogService = auditLogService;
    }

    @PostMapping("/login")
    public AuthTokenResponse login(@Valid @RequestBody AuthLoginRequest request) {
        UserEntity user = userRepository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid username or password");
        }
        String token = jwtService.generateToken(user.getUsername());
        auditLogService.userAction(AuditAction.LOGIN, "USER", user.getId(), "User logged in");
        return new AuthTokenResponse(token, "Bearer", jwtService.expirationSeconds());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        tokenBlacklist.block(tokenFrom(request));
        auditLogService.userAction(AuditAction.LOGOUT, "USER", currentUserService.currentUserIdOrNull(), "User logged out");
    }

    @GetMapping("/me")
    public UserResponse me() {
        return currentUserService.currentUser()
                .map(UserResponse::from)
                .orElseThrow(() -> new BadRequestException("No authenticated user"));
    }

    private String tokenFrom(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return header != null && header.startsWith("Bearer ") ? header.substring(7) : null;
    }
}
