package com.example.projectmanagement.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ExcelImportResultDto {
    private Long importId;
    private Long projectId;
    private String fileName;
    private int epicsCreated;
    private int storiesCreated;
    private int tasksCreated;
    private String status;
    private List<String> errors;
    private LocalDateTime uploadedAt;
}
