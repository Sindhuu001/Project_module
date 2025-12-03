package com.example.projectmanagement.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cglib.core.Local;

import jakarta.persistence.Column;

@Data
public class StoryViewDto {

    private Long id;

    private String title;
    private String description;
    private String acceptanceCriteria;

    private Integer storyPoints;
    private String priority;

    // Status
    private Long statusId;
    private String statusName;

    // Epic
    private Long epicId;
    private String epicTitle;

    // Project
    private Long projectId;
    private String projectName;

    // Sprint
    private Long sprintId;
    private String sprintName;

    // Users
    private Long assigneeId;
    private String assigneeName;

    private Long reporterId;
    private String reporterName;

    // Associated tasks
    private List<Long> taskIds;
   
    private LocalDateTime startDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueDate;
}
