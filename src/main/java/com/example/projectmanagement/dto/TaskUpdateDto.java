package com.example.projectmanagement.dto;


import com.example.projectmanagement.entity.Task;



import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TaskUpdateDto {
    private String title;
    private String description;
    private Task.Priority priority;
    private Integer storyPoints;
    private LocalDateTime dueDate;
    private Boolean billable;
    private Long assigneeId;
    private Long statusId;
    private Long storyId;
    private Long sprintId;
    private Long reporterId;
    public LocalDateTime getStartDate() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStartDate'");
    }

    // Getters & Setters
}

