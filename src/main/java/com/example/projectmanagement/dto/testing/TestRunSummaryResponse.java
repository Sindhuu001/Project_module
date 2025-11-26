package com.example.projectmanagement.dto.testing;

public record TestRunSummaryResponse(
        Long id,
        String name,
        String status,
        String description,
        Long cycleId,
        int caseCount,       // assigned cases count
        int completedCount   // % calculation possible later
) {}
