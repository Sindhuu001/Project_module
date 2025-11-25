package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.RiskLinkRequest;
import com.example.projectmanagement.dto.RiskLinkResponse;

import java.util.List;

public interface RiskLinkService {
    RiskLinkResponse createLink(RiskLinkRequest request);
    RiskLinkResponse updateLink(Long id, RiskLinkRequest request);
    void deleteLink(Long id);
    List<RiskLinkResponse> getLinksByRiskId(Long riskId);
}
