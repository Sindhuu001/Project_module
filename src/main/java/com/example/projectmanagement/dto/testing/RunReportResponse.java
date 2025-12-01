package com.example.projectmanagement.dto.testing;

import java.util.List;

public record RunReportResponse(
        Long runId,
        String name,
        int totalCases,
        int passed,
        int failed,
        int blocked,
        int skipped,
        List<TopFailingScenario> topFailing
) {
    public record TopFailingScenario(Long scenarioId, String title, int failureCount) {}
}
