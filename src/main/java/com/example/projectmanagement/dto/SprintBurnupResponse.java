package com.example.projectmanagement.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SprintBurnupResponse {

    private Long sprintId;
    private String sprintName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalSprintDays;

    // Baseline totals
    private Integer initialStoryPoints;      // original commitment — never changes
    private Integer currentStoryPoints;      // live total right now (scope may have changed)
    private Integer completedStoryPoints;    // done right now
    private Integer remainingStoryPoints;    // currentStoryPoints - completedStoryPoints

    // Issue counts
    private Integer totalIssues;
    private Integer completedIssues;
    private Integer remainingIssues;

    // Sprint health summary
    private Integer completionPercentage;    // (completedStoryPoints / currentStoryPoints) * 100
    private Boolean isOnTrack;               // completedPoints >= idealCompletedPoints for today
    private Integer deviationPoints;         // positive = ahead, negative = behind (burnup convention)

    // Per-day data for the chart
    private List<DailyBurnup> dailyBurnup;

    // Audit events — shown as markers on the chart
    private List<ScopeChangeEvent> scopeChanges;

    @Data
    public static class DailyBurnup {

        private LocalDate date;
        private Integer sprintDayNumber;
        private Boolean isWeekend;

        // ── The two main burnup lines ──────────────────────────────

        private Integer completedPoints;        // actual line — goes UP as work is done
        private Integer totalScopePoints;       // scope line — flat unless stories added/removed

        // ── Reference lines ───────────────────────────────────────

        private Integer initialScopePoints;     // original commitment — always flat (for reference)
        private Integer idealCompletedPoints;   // where team SHOULD be today (linear from 0 → initial)

        // ── Deviation ─────────────────────────────────────────────

        private Integer deviationPoints;        // completedPoints - idealCompletedPoints
                                                // positive = ahead, negative = behind

        // ── Daily velocity ────────────────────────────────────────

        private Integer velocityPoints;         // points completed TODAY only (bar chart)
        private Integer velocityIssues;         // issues closed TODAY only

        // ── Scope change (for markers on chart) ───────────────────

        private Integer addedScopePoints;       // scope added TODAY
        private Integer removedScopePoints;     // scope removed TODAY

        // ── Issue counts ──────────────────────────────────────────

        private Integer completedIssues;
        private Integer totalIssues;
        private Integer remainingIssues;

        private Boolean isHoliday;
        private Boolean isWorkingWeekend;

        // null values = future date (frontend should not plot these)
    }

    @Data
    public static class ScopeChangeEvent {
        private LocalDate date;
        private Integer sprintDayNumber;
        private String issueTitle;
        private String changeType;
        private Integer pointsDelta;
        private Long changedBy;
        private Long epicId;
        private String epicName;
    }
}