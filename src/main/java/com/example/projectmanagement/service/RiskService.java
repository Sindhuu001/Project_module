package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.RiskRequest;
import com.example.projectmanagement.dto.RiskResponse;
import com.example.projectmanagement.dto.RiskStatusUpdateRequest;

import java.util.List;

public interface RiskService {

    // Create
    RiskResponse createRisk(RiskRequest request);

    // Read all risks by project
    List<RiskResponse> getRisksByProject(Long projectId);

    // Read single risk
    RiskResponse getRiskById(Long id);

    // Update full risk
    RiskResponse updateRisk(Long id, RiskRequest request);

    // Delete risk
    void deleteRisk(Long id);

    // Patch: Update status
    RiskResponse updateStatus(Long id, RiskStatusUpdateRequest request);
}
