package com.example.projectmanagement.dto.testing;

public record AssignmentConflictItem(
        Long testCaseId,
        Long existingRunCaseId, // existing TestRunCase id if present
        Long assignedTo // existing assigneeId (nullable)
) {}
