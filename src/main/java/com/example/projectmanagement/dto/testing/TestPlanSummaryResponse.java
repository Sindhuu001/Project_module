package com.example.projectmanagement.dto.testing;

import java.time.LocalDateTime;

public record TestPlanSummaryResponse(
        Long id,
        String name,
        String objective,
        LocalDateTime createdAt,
        int scenarioCount,
        int caseCount,
        int coveredStoryCount
) {}
