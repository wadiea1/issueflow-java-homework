package com.att.tdp.issueflow.controller;

import com.att.tdp.issueflow.dto.*;
import com.att.tdp.issueflow.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final CommentService commentService;
    private final DependencyService dependencyService;
    private final AttachmentService attachmentService;

    public TicketController(TicketService ticketService, CommentService commentService, DependencyService dependencyService, AttachmentService attachmentService) {
        this.ticketService = ticketService;
        this.commentService = commentService;
        this.dependencyService = dependencyService;
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public List<TicketResponse> list(@RequestParam Long projectId) {
        return ticketService.listByProject(projectId);
    }

    @GetMapping("/deleted")
    public List<TicketResponse> deleted(@RequestParam Long projectId) {
        return ticketService.deletedByProject(projectId);
    }

    @GetMapping("/{ticketId}")
    public TicketResponse get(@PathVariable Long ticketId) {
        return ticketService.get(ticketId);
    }

    @PostMapping
    public TicketResponse create(@Valid @RequestBody TicketRequest request) {
        return ticketService.create(request);
    }

    @PatchMapping("/{ticketId}")
    public void update(@PathVariable Long ticketId, @RequestBody TicketUpdateRequest request) {
        ticketService.update(ticketId, request);
    }

    @DeleteMapping("/{ticketId}")
    public void delete(@PathVariable Long ticketId) {
        ticketService.softDelete(ticketId);
    }

    @PostMapping("/{ticketId}/restore")
    public void restore(@PathVariable Long ticketId) {
        ticketService.restore(ticketId);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(@RequestParam Long projectId) {
        byte[] bytes = ticketService.exportCsv(projectId).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tickets-project-" + projectId + ".csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(bytes);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportSummaryResponse importCsv(@RequestParam Long projectId, @RequestPart("file") MultipartFile file) {
        return ticketService.importCsv(projectId, file);
    }

    @GetMapping("/{ticketId}/comments")
    public List<CommentResponse> comments(@PathVariable Long ticketId) {
        return commentService.list(ticketId);
    }

    @PostMapping("/{ticketId}/comments")
    public CommentResponse addComment(@PathVariable Long ticketId, @Valid @RequestBody CommentRequest request) {
        return commentService.add(ticketId, request);
    }

    @PatchMapping("/{ticketId}/comments/{commentId}")
    public void updateComment(@PathVariable Long ticketId, @PathVariable Long commentId, @Valid @RequestBody CommentUpdateRequest request) {
        commentService.update(ticketId, commentId, request);
    }

    @DeleteMapping("/{ticketId}/comments/{commentId}")
    public void deleteComment(@PathVariable Long ticketId, @PathVariable Long commentId) {
        commentService.delete(ticketId, commentId);
    }

    @PostMapping("/{ticketId}/dependencies")
    public void addDependency(@PathVariable Long ticketId, @Valid @RequestBody DependencyRequest request) {
        dependencyService.add(ticketId, request.blockedBy());
    }

    @GetMapping("/{ticketId}/dependencies")
    public List<DependencyResponse> dependencies(@PathVariable Long ticketId) {
        return dependencyService.list(ticketId);
    }

    @DeleteMapping("/{ticketId}/dependencies/{blockerId}")
    public void removeDependency(@PathVariable Long ticketId, @PathVariable Long blockerId) {
        dependencyService.remove(ticketId, blockerId);
    }

    @PostMapping(value = "/{ticketId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AttachmentResponse uploadAttachment(@PathVariable Long ticketId, @RequestPart("file") MultipartFile file) {
        return attachmentService.upload(ticketId, file);
    }

    @DeleteMapping("/{ticketId}/attachments/{attachmentId}")
    public void deleteAttachment(@PathVariable Long ticketId, @PathVariable Long attachmentId) {
        attachmentService.delete(ticketId, attachmentId);
    }
}
