package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.RiskAttachmentRequest;
import com.example.projectmanagement.dto.RiskAttachmentResponse;
import com.example.projectmanagement.entity.Risk;
import com.example.projectmanagement.entity.RiskAttachment;
import com.example.projectmanagement.repository.RiskAttachmentRepository;
import com.example.projectmanagement.repository.RiskRepository;
import com.example.projectmanagement.service.RiskAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RiskAttachmentServiceImpl implements RiskAttachmentService {

    @Autowired
    private RiskAttachmentRepository attachmentRepository;

    @Autowired
    private RiskRepository riskRepository;

    @Override
    public RiskAttachmentResponse uploadAttachment(RiskAttachmentRequest request) {
        Risk risk = riskRepository.findById(request.getRiskId())
                .orElseThrow(() -> new IllegalArgumentException("Risk not found"));

        RiskAttachment attachment = new RiskAttachment();
        attachment.setRisk(risk);
        attachment.setFileName(request.getFileName());
        attachment.setFileType(request.getFileType());
        attachment.setFileData(request.getFileData());
        attachment.setUploadedAt(LocalDateTime.now());

        return toResponse(attachmentRepository.save(attachment));
    }

    @Override
    public List<RiskAttachmentResponse> getAttachmentsByRiskId(Long riskId) {
        Risk risk = riskRepository.findById(riskId)
                .orElseThrow(() -> new IllegalArgumentException("Risk not found"));

        return attachmentRepository.findByRisk(risk)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAttachment(Long id) {
        RiskAttachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));
        attachmentRepository.delete(attachment);
    }

    private RiskAttachmentResponse toResponse(RiskAttachment attachment) {
        RiskAttachmentResponse response = new RiskAttachmentResponse();
        response.setId(attachment.getId());
        response.setRiskId(attachment.getRisk().getId());
        response.setFileName(attachment.getFileName());
        response.setFileType(attachment.getFileType());
        response.setUploadedAt(attachment.getUploadedAt());
        return response;
    }
}
