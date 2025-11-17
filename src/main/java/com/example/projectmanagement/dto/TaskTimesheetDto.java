package com.example.projectmanagement.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskTimesheetDto {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean billable;

    private ProjectSmallDto project;   // nested project object
}
