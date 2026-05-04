package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.RiskAttachmentRequest;
import com.example.projectmanagement.dto.RiskAttachmentResponse;
import com.example.projectmanagement.service.RiskAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;

@RestController
@RequestMapping("/api/risk-attachments")
public class RiskAttachmentController {

    @Autowired
    private RiskAttachmentService attachmentService;

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")

    public ResponseEntity<RiskAttachmentResponse> upload(@RequestBody RiskAttachmentRequest request) {
        return ResponseEntity.ok(attachmentService.uploadAttachment(request));
    }

    @GetMapping("/risk/{riskId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<RiskAttachmentResponse>> getByRisk(@PathVariable Long riskId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByRiskId(riskId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }
}
