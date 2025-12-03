package com.example.projectmanagement.dto.testing;

public record TestCaseSummaryResponse(
        Long id,
        String title,
        String type,
        String priority,
        String status,
        int stepCount
) {}