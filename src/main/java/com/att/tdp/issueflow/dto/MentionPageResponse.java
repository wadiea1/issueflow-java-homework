package com.att.tdp.issueflow.dto;

import java.util.List;

public record MentionPageResponse(List<CommentResponse> data, long total, int page) {}
