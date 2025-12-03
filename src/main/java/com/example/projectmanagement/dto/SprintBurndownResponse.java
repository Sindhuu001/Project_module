package com.example.projectmanagement.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SprintBurndownResponse {
    private Long sprintId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalStoryPoints;

    private List<DailyBurn> dailyBurn;

    @Data
    public static class DailyBurn {
        private LocalDate date;
        private Integer remaining;
    }
}
