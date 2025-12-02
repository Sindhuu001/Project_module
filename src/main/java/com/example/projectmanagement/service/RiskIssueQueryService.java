package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.RiskIssueSummaryDTO;
import com.example.projectmanagement.entity.RiskLink.LinkedType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RiskIssueQueryService {

    Page<RiskIssueSummaryDTO> getIssuesWithRisks(
            Long projectId,
            LinkedType issueType,
            String issueStatus,
            Long sprintId,
            Pageable pageable
    );
}
