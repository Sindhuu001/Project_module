package com.example.projectmanagement.dto;

import com.example.projectmanagement.entity.Status;
import com.example.projectmanagement.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Full Task DTO – includes all fields for detailed payloads.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    private Long id;

    // === Basic Task Fields ===
    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 200, message = "Task title must be between 2 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    private Status status;
    private Task.Priority priority;
    private Integer storyPoints;
    private LocalDateTime dueDate;
    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Reporter ID is required")
    private Long reporterId;

    private Long assigneeId;
    private Long storyId;
    private Long sprintId;

    // === Nested DTOs (for full relational data) ===
    private UserDto reporter;     // Full reporter info (id, name, username, email, etc.)
    private UserDto assignee;     // Full assignee info
    private ProjectDto project;   // Full project info
    private StoryDto story;       // Linked story (if any)
    private SprintDto sprint;     // Linked sprint (if any)

    // === Optional additional info (useful for frontend display) ===
    private String reporterName;
    private String assigneeName;
    private String projectName;
    private String sprintName;
    private String storyTitle;

    // === Convenience constructor for lightweight creation ===
    public TaskDto(String title, String description, Long projectId, Long reporterId) {
        this.title = title;
        this.description = description;
        this.projectId = projectId;
        this.reporterId = reporterId;
    }

    // ✅ Optional Summary inner class for lightweight responses
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String title;
        private Status status;
        private Long storyId;
        private Long sprintId;
        private Task.Priority priority;
        private Long reporterId;
        private String reporterName;
        private Long assigneeId;
        private String assigneeName;
        private LocalDateTime createdAt;
        private boolean billable;
        private LocalDateTime dueDate;

        // ✅ Constructor for 10-argument query (project-level)
        public Summary(Long id, String title, Status status,
                    Long storyId, Long sprintId, Task.Priority priority,
                    Long reporterId, Long assigneeId,
                    LocalDateTime createdAt, boolean billable) {
            this.id = id;
            this.title = title;
            this.status = status;
            this.storyId = storyId;
            this.sprintId = sprintId;
            this.priority = priority;
            this.reporterId = reporterId;
            this.assigneeId = assigneeId;
            this.createdAt = createdAt;
            this.billable = billable;
        }

        // ✅ Constructor for 5-argument query (sprint-level)
        public Summary(Long id, String title, Status status,
                    Long storyId, Long sprintId) {
            this.id = id;
            this.title = title;
            this.status = status;
            this.storyId = storyId;
            this.sprintId = sprintId;
        }
    }

    public void setBillable(boolean billable) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setBillable'");
    }

    public void setStartDate(LocalDateTime startDate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setStartDate'");
    }

}
         