package com.example.projectmanagement.dto.testing;

public record TestRunCaseResponse(
        Long testCaseId,
        String title,
        String type,
        String priority,
        String status, // This is the status from the TestCase entity
        Long assigneeId,
        String runStatus // This is the status from the TestRunCase entity
) {}
