package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.model.Role;
import com.att.tdp.issueflow.model.UserEntity;

public record UserResponse(Long id, String username, String email, String fullName, Role role) {
    public static UserResponse from(UserEntity user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.getRole());
    }
}
