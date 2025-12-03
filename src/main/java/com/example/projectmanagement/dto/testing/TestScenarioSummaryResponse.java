package com.example.projectmanagement.dto.testing;

public record TestScenarioSummaryResponse(
        Long id,
        String title,
        String priority,
        String status,
        Long testStoryId,
        Long linkedStoryId,
        int caseCount
) {}