package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.RiskStatusCreateRequest;
import com.example.projectmanagement.dto.RiskStatusResponse;
import com.example.projectmanagement.entity.RiskStatus;

import java.util.List;
import java.util.Map;

public interface RiskStatusService {

    RiskStatusResponse createRiskStatus(RiskStatusCreateRequest request);

    List<RiskStatusResponse> getRiskStatusesByProject(Long projectId);

    RiskStatus getRiskStatus(Long id);

    RiskStatusResponse updateRiskStatus(Long id, RiskStatusCreateRequest request);

    void deleteRiskStatus(Long id, Long newStatusId);

    List<RiskStatusResponse> reorderRiskStatuses(Map<Long, Integer> newOrder);
    void createDefaultStatusesForProject(Long projectId);


}
