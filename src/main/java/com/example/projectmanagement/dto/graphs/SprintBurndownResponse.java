package com.example.projectmanagement.dto.graphs;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SprintBurndownResponse {

    private Long sprintId;
    private String sprintName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalSprintDays;

    // Story point totals
    private Integer initialStoryPoints;       // ideal line anchor
    private Integer currentStoryPoints;       // may differ from initial if scope changed
    private Integer completedStoryPoints;
    private Integer remainingStoryPoints;

    // Issue counts
    private Integer totalIssues;
    private Integer completedIssues;
    private Integer remainingIssues;

    // Per-day data
    private List<DailyBurn> dailyBurn;

    // Scope change events (for chart markers)
    private List<ScopeChangeEvent> scopeChanges;

    @Data
    public static class DailyBurn {
        private LocalDate date;
        private Integer sprintDayNumber;
        private Boolean isWeekend;

        // Actual burndown
        private Integer remainingStoryPoints;   // actual line
        private Integer idealRemainingPoints;   // ideal line
        private Integer deviationPoints;        // positive = behind, negative = ahead

        // Completed
        private Integer completedStoryPoints;
        private Integer velocityPoints;         // burned on this day only

        // Issues
        private Integer remainingIssues;
        private Integer completedIssues;
        private Integer velocityIssues;

        // Scope
        private Integer addedScopePoints;
        private Integer removedScopePoints;
         private Boolean isHoliday;           // ← add
    private Boolean isWorkingWeekend;    // ← add

        

        // null = future date (not yet snapshotted)
        // populated = past/today
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