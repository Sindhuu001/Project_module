package com.example.projectmanagement.dto;

import com.example.projectmanagement.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
public class TaskDto {
    private Long id;
    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 200, message = "Task title must be between 2 and 200 characters")
    private String title;
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    private Task.TaskStatus status;
    private Task.Priority priority;
    private Integer storyPoints;
    private LocalDateTime dueDate;
    @NotNull(message = "Project ID is required")
    private Long projectId;
    @NotNull(message = "Reporter ID is required")
    private Long reporterId;
    private Long storyId;
    private Long sprintId;
    private Long assigneeId;
    private UserDto assignee;
    private UserDto reporter;
    private StoryDto story;
    private SprintDto sprint;
    private ProjectDto project;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isBillable;

    public TaskDto() {}

    public TaskDto(String title, String description, Long projectId, Long reporterId) {
        this.title = title;
        this.description = description;
        this.projectId = projectId;
        this.reporterId = reporterId;
    }

    // ✅ Inner summary DTO for lightweight responses
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String title;
        private Task.TaskStatus status;
        private Long storyId;
        private Long sprintId;
        private Task.Priority priority;
        private Long reporterId;
        private String reporterName;
        private Long assigneeId;
        private String assigneeName;
        private LocalDateTime createdAt;
        private boolean isBillable;

        // ✅ Constructor for 10-argument query (project-level)
        public Summary(Long id, String title, Task.TaskStatus status,
                    Long storyId, Long sprintId, Task.Priority priority,
                    Long reporterId, Long assigneeId,
                    LocalDateTime createdAt, boolean isBillable) {
            this.id = id;
            this.title = title;
            this.status = status;
            this.storyId = storyId;
            this.sprintId = sprintId;
            this.priority = priority;
            this.reporterId = reporterId;
            this.assigneeId = assigneeId;
            this.createdAt = createdAt;
            this.isBillable = isBillable;
        }

        // ✅ Constructor for 5-argument query (sprint-level)
        public Summary(Long id, String title, Task.TaskStatus status,
                    Long storyId, Long sprintId) {
            this.id = id;
            this.title = title;
            this.status = status;
            this.storyId = storyId;
            this.sprintId = sprintId;
        }
    }

}
