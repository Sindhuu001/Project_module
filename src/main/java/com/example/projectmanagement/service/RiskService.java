package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.RiskRequest;
import com.example.projectmanagement.dto.RiskResponse;
import com.example.projectmanagement.dto.RiskResponseDTO;
import com.example.projectmanagement.dto.RiskStatusUpdateRequest;
import com.example.projectmanagement.entity.RiskLink;

import java.util.List;

public interface RiskService {

    // ===== EXISTING (UNCHANGED) =====
    RiskResponse createRisk(RiskRequest request);
    List<RiskResponse> getRisksByProject(Long projectId);
    RiskResponse getRiskById(Long id);
    RiskResponse updateRisk(Long id, RiskRequest request);
    void deleteRisk(Long id);
    RiskResponse updateStatus(Long id, RiskStatusUpdateRequest request);

    // ===== ✅ NEW (ADDED) =====
    RiskResponseDTO getRisksWithPagination(
            Long projectId,
            RiskLink.LinkedType linkedType,
            Long linkedId,
            int page,
            int size,
            String severityFilter
    );
}
