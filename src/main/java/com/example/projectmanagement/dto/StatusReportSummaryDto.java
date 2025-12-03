package com.example.projectmanagement.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.projectmanagement.entity.Project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusReportSummaryDto {

    private String projectName;
    private String owner;
    private Project.ProjectStatus status;
    private Project.ProjectStage currenStage;
    private List<String> members;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Double OverallPercentage;
    private Integer totalEpics;
    private Integer totalStories;
    private Integer totalTasks;
}
