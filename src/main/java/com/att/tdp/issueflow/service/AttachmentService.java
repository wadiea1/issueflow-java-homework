package com.att.tdp.issueflow.service;

import com.att.tdp.issueflow.dto.AttachmentResponse;
import com.att.tdp.issueflow.exception.BadRequestException;
import com.att.tdp.issueflow.exception.NotFoundException;
import com.att.tdp.issueflow.model.AttachmentEntity;
import com.att.tdp.issueflow.model.AuditAction;
import com.att.tdp.issueflow.model.TicketEntity;
import com.att.tdp.issueflow.repository.AttachmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
public class AttachmentService {
    private static final long MAX_SIZE = 10L * 1024L * 1024L;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg", "application/pdf", "text/plain");

    private final AttachmentRepository attachmentRepository;
    private final TicketService ticketService;
    private final AuditLogService auditLogService;

    public AttachmentService(AttachmentRepository attachmentRepository, TicketService ticketService, AuditLogService auditLogService) {
        this.attachmentRepository = attachmentRepository;
        this.ticketService = ticketService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AttachmentResponse upload(Long ticketId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BadRequestException("Maximum file size is 10 MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("File type is not allowed");
        }
        try {
            TicketEntity ticket = ticketService.find(ticketId);
            AttachmentEntity attachment = new AttachmentEntity();
            attachment.setTicket(ticket);
            attachment.setFilename(file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename());
            attachment.setContentType(file.getContentType());
            attachment.setSizeBytes(file.getSize());
            attachment.setData(file.getBytes());
            AttachmentEntity saved = attachmentRepository.save(attachment);
            auditLogService.userAction(AuditAction.UPLOAD_ATTACHMENT, "ATTACHMENT", saved.getId(), "Uploaded attachment");
            return AttachmentResponse.from(saved);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Could not save file: " + e.getMessage());
        }
    }

    @Transactional
    public void delete(Long ticketId, Long attachmentId) {
        AttachmentEntity attachment = attachmentRepository.findByIdAndTicket_Id(attachmentId, ticketId)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));
        attachmentRepository.delete(attachment);
        auditLogService.userAction(AuditAction.DELETE_ATTACHMENT, "ATTACHMENT", attachmentId, "Deleted attachment");
    }
}
