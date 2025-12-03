package com.example.projectmanagement.dto.testing;

import java.time.LocalDateTime;
import java.util.List;

public record CaseReportResponse(
        Long testCaseId,
        String title,
        String lastRunStatus,
        List<RunHistoryItem> history,
        int totalBugs,
        double flakyScore
) {
    public record RunHistoryItem(Long runId, LocalDateTime executedAt, String status) {}
}
