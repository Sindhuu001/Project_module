package com.example.projectmanagement.dto.testing;

import java.time.LocalDate;
import java.util.List;

public record CycleReportResponse(
        Long cycleId,
        String name,
        int totalRuns,
        int totalCases,
        double passRate,
        List<RunTrend> trend
) {
    public record RunTrend(LocalDate date, int passed, int failed) {}
}
