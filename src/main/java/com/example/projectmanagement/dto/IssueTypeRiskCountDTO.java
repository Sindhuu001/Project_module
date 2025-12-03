package com.example.projectmanagement.dto;

import com.example.projectmanagement.entity.RiskLink.LinkedType;
import lombok.Data;

@Data
public class IssueTypeRiskCountDTO {

    private LinkedType issueType;
    private Long riskCount;

    public IssueTypeRiskCountDTO(LinkedType issueType, Long riskCount) {
        this.issueType = issueType;
        this.riskCount = riskCount;
    }
}

