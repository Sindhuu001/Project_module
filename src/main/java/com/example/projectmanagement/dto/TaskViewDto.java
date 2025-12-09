package com.example.projectmanagement.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.projectmanagement.entity.Task;

@Data
public class TaskViewDto {

    private Long id;

    // Project
    private Long projectId;
    private String projectName;

    // Status
    private Long statusId;
    private String statusName;

    // Story
    private Long storyId;
    private String storyTitle;

    // Sprint (auto derived from story)
    private Long sprintId;
    private String sprintName;

    // Assignee & Reporter
    private Long assigneeId;
    private String assigneeName;

    private Long reporterId;
    private String reporterName;

    // Task fields
    private String title;
    private String description;
    private Integer storyPoints;
    private LocalDateTime dueDate;
    private boolean billable;
    private Task.Priority priority;
   private Long createdBy;
    private LocalDateTime startDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
