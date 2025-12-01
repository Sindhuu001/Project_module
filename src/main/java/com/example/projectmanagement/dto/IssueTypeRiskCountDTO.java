package com.example.projectmanagement.dto;

import java.util.List;

import lombok.Data;

@Data
public class IssueTypeRiskCountDTO {

    private String issueType;
    private Long riskCount;

    public IssueTypeRiskCountDTO(String issueType, Long riskCount) {
        this.issueType = issueType;
        this.riskCount = riskCount;
    }

    public String getIssueType() {
        return issueType;
    }

    public Long getRiskCount() {
        return riskCount;
    }
}

