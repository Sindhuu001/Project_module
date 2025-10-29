package com.example.projectmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    private Long totalProjects;
    private Long totalTasks;
    private Map<String, Long> taskStatusCount;
    private Long totalEpics;
    private Map<String, Long> epicStatusCount;
    private Long totalStories;
    private Map<String, Long> storyStatusCount; 
    //private Long totalUsers;
    private List<?> projects;
    private List<?> stories;
    private List<?> tasks;

}
    

