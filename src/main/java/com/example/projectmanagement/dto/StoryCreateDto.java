package com.example.projectmanagement.dto;

import java.time.LocalDate;

import com.example.projectmanagement.entity.Story;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StoryCreateDto {

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    private String acceptanceCriteria;

    private Integer storyPoints;

    private Long assigneeId;

    @NotNull(message = "Reporter is required")
    private Long reporterId;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long epicId;

    private Long sprintId;
   
   private LocalDateTime startDate;

    @NotNull(message = "Status ID is required")
    private Long statusId;

    @NotNull(message = "Priority is required")
    private Story.Priority priority;  // LOW / MEDIUM / HIGH / CRITICAL
    private LocalDateTime dueDate;
}
