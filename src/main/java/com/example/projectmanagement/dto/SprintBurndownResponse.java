package com.example.projectmanagement.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import com.example.projectmanagement.dto.graphs.SprintBurndownResponse.ScopeChangeEvent;

@Data
public class SprintBurndownResponse {

    private Long sprintId;
    private String sprintName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalSprintDays;
    private Integer totalStoryPoints;

    // Summary fields
    private Integer initialStoryPoints;
    private Integer currentStoryPoints;
    private Integer completedStoryPoints;
    private Integer remainingStoryPoints;
    private Integer totalIssues;
    private Integer completedIssues;
    private Integer remainingIssues;

    private List<DailyBurn> dailyBurn;
    private List<ScopeChangeEvent> scopeChanges;

    @Data
    public static class DailyBurn {
        private LocalDate date;
        private Integer sprintDayNumber;
        private Boolean isWeekend;
        private Integer remaining;
        private Integer totalStoryPoints;
        // Ideal line
        private Integer idealRemainingPoints;
        private Integer deviationPoints;

        // Actuals (null for future days)
        private Integer remainingStoryPoints;
        private Integer completedStoryPoints;
        private Integer velocityPoints;
        private Integer addedScopePoints;
        private Integer removedScopePoints;
        private Integer remainingIssues;
        private Integer completedIssues;
        private Integer velocityIssues;
        private Integer addedScopeIssues;
        private Integer removedScopeIssues;
        private Boolean isHoliday;
        private Boolean isWorkingWeekend;
    }
}
