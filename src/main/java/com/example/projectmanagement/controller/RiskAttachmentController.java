package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.RiskAttachmentRequest;
import com.example.projectmanagement.dto.RiskAttachmentResponse;
import com.example.projectmanagement.service.RiskAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk-attachments")
public class RiskAttachmentController {

    @Autowired
    private RiskAttachmentService attachmentService;

    @PostMapping("/upload")
    public ResponseEntity<RiskAttachmentResponse> upload(@RequestBody RiskAttachmentRequest request) {
        return ResponseEntity.ok(attachmentService.uploadAttachment(request));
    }

    @GetMapping("/risk/{riskId}")
    public ResponseEntity<List<RiskAttachmentResponse>> getByRisk(@PathVariable Long riskId) {
        return ResponseEntity.ok(attachmentService.getAttachmentsByRiskId(riskId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }
}
