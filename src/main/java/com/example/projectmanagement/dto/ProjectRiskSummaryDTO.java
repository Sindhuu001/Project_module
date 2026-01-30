package com.example.projectmanagement.dto;

import lombok.Data;

@Data
public class ProjectRiskSummaryDTO {

    private Long projectId;

    private Long sprintId;

    private int totalActiveRisks;

    private int highRisks;

    private int mediumRisks;

    private int lowRisks;

    private int totalRiskScore;

    private int maxRiskScore;

    /**
     * Derived indicator (computed, not stored)
     */
    private RiskHealth riskHealth;

    public enum RiskHealth {
        LOW,
        MEDIUM,
        HIGH
    }
}

