package com.example.projectmanagement.dto;

public record RiskIssueSummaryDTO(
        String linkedType,
        Long linkedId,
        String title,
        String issueStatus,
        Long sprintId,
        Long riskCount
) {}

