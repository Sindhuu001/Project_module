package com.example.projectmanagement.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestWorkItemDto {

    private Long   id;
    private String type;          // "TEST_RUN" | "TEST_CASE"
    private String title;
    private String status;        // TestRunStatus or TestRunCaseStatus as string
    private Long   projectId;
    private String projectName;

    // Run context (for test cases)
    private Long   runId;
    private String runName;
    private String cycleName;

    // Run-specific
    private int    totalCases;
    private int    remainingCases;

    private LocalDateTime createdAt;
    private LocalDateTime lastExecutedAt;
}