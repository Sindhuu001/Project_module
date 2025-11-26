package com.example.projectmanagement.dto.testing;

public record TestStepResponse(
        Long id,
        int stepNumber,
        String action,
        String expectedResult
) {}
