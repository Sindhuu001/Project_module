package com.example.projectmanagement.dto;

import com.example.projectmanagement.entity.Project;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardSummaryDto {

    // Overall counts
    private Long totalProjects;
    private Long totalTasks;
    private Long totalEpics;
    private Long totalStories;

    // For user-specific summary
    private List<Project> projectsInvolved;
    private Long tasksAssigned;
    private Long storiesAssigned;

    // Status counts
    private Map<String, Long> taskStatusCount;
    private Map<String, Long> storyStatusCount;
    private Map<String, Long> epicStatusCount;
}
