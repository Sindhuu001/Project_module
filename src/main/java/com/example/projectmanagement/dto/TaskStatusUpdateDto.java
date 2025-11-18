package com.example.projectmanagement.dto;

import lombok.Data;

@Data
public class TaskStatusUpdateDto {
    private Long id;
    private Long statusId;
    private String statusName;
    private Long storyId;
    private Long sprintId;
}
