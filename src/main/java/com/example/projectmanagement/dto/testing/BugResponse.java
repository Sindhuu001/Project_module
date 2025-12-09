package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.BugPriority;
import com.example.projectmanagement.enums.BugSeverity;
import com.example.projectmanagement.enums.BugStatus;
import com.example.projectmanagement.enums.BugType;

import java.time.LocalDateTime;

public record BugResponse(
        Long id,
        String title,
        BugStatus status,
        BugSeverity severity,
        BugPriority priority,
        BugType type,
        Long reporterId,
        Long assignedTo,
        Long runId,
        Long runCaseId,
        Long runCaseStepId,
        Long testCaseId,
        Long testScenarioId,
        Long testStoryId,
        Long projectId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
