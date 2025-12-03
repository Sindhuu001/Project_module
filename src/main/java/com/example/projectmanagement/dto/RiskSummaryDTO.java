package com.example.projectmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskSummaryDTO {
    private int totalRisks;
    private int highSeverityCount;
    private double avgRiskScore;
}
