package com.example.projectmanagement.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugDto {
    private Long id;
    private String title;
    private String description;
    private String priority;   // HIGH, LOW, etc.
    private String status;     // OPEN, IN_PROGRESS, etc.
    private String severity;   // MAJOR, MINOR, etc.
    private String type;
    private Long assignedTo;
    private Long reporter;
    private Long projectId;
    private Long sprintId;
    private Long epicId;
    private Long taskId;
    private String stepsToReproduce;
    private String expectedResult;
    private String actualResult;
    private String attachments;
    private String createdDate;
    private String updatedDate;
    private String resolvedDate;
}