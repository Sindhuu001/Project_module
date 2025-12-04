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
@Data
public class TaskCreateDto {

    private Long id;

    @NotBlank(message = "Task title is required")
    @Size(min = 2, max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    private Long statusId;   // <-- USE THIS

    private Task.Priority priority;
    private Integer storyPoints;
    private LocalDateTime dueDate;

    @NotNull private Long projectId;
    @NotNull private Long reporterId;
    private Long storyId;
    private Long assigneeId;
    private Long sprintId;

    private boolean isBillable;
    private LocalDateTime startDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
