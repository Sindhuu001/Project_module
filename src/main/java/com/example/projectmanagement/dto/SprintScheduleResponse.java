package com.example.projectmanagement.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class SprintScheduleResponse {
    private Long sprintId;
    private String sprintName;
    private List<LocalDate> holidays;
    private List<LocalDate> workingWeekends;
}
