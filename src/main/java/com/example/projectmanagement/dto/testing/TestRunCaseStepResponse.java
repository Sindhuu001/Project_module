package com.example.projectmanagement.dto.testing;
public record TestRunCaseStepResponse(
        Long id,
        Long stepId,
        Integer stepNumber,
        String action,
        String expectedResult,
        String status,
        String actualResult
) {}

