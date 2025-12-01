package com.example.projectmanagement.dto.testing;

public record PlanReportResponse(Long planId, String name,
                                 int totalStories, int totalScenarios, int totalCases,
                                 int executedCases, int passedCases, int failedCases, double passRate,
                                 Long latestCycleId) {}