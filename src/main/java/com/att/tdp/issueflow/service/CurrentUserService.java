package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.model.UserEntity;
import com.att.tdp.issueflow.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserEntity> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
            return Optional.empty();
        }
        return userRepository.findByUsernameIgnoreCase(auth.getName());
    }

    public Long currentUserIdOrNull() {
        return currentUser().map(UserEntity::getId).orElse(null);
    }
}
