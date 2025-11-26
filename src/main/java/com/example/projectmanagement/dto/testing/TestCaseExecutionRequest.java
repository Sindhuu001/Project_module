package com.example.projectmanagement.dto.testing;

import jakarta.validation.constraints.NotNull;

public record TestCaseExecutionRequest(
        @NotNull Long runCaseId,
        Long stepId,             // Optional (only for fail)
        String actualResult,
        String comment
) {}

