package com.att.tdp.issueflow.dto;

import com.att.tdp.issueflow.model.AttachmentEntity;

public record AttachmentResponse(Long id, Long ticketId, String filename, String contentType) {
    public static AttachmentResponse from(AttachmentEntity attachment) {
        return new AttachmentResponse(attachment.getId(), attachment.getTicket().getId(), attachment.getFilename(), attachment.getContentType());
    }
}
