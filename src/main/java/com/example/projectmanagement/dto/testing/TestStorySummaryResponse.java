package com.example.projectmanagement.dto.testing;

public record TestStorySummaryResponse(
        Long id,
        String name,
        Long linkedStoryId,
        int scenarioCount
) {}
