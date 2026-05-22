package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.model.CommentEntity;

import java.util.List;

public record CommentResponse(
        Long id,
        Long ticketId,
        Long authorId,
        String content,
        List<MentionedUserResponse> mentionedUsers
) {
    public static CommentResponse from(CommentEntity comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicket().getId(),
                comment.getAuthor().getId(),
                comment.getContent(),
                comment.getMentionedUsers().stream().map(MentionedUserResponse::from).toList()
        );
    }
}
