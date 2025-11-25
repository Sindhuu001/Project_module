package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.RiskAttachmentRequest;
import com.example.projectmanagement.dto.RiskAttachmentResponse;

import java.util.List;

public interface RiskAttachmentService {
    RiskAttachmentResponse uploadAttachment(RiskAttachmentRequest request);
    List<RiskAttachmentResponse> getAttachmentsByRiskId(Long riskId);
    void deleteAttachment(Long id);
}
