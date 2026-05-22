package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.model.UserEntity;

public record MentionedUserResponse(Long id, String username, String fullName) {
    public static MentionedUserResponse from(UserEntity user) {
        return new MentionedUserResponse(user.getId(), user.getUsername(), user.getFullName());
    }
}
