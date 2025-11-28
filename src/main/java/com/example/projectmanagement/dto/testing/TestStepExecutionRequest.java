package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.TestStepResultStatus;
import jakarta.validation.constraints.NotNull;

public record TestStepExecutionRequest(
        @NotNull Long runCaseId,
        @NotNull Long stepId,
        @NotNull TestStepResultStatus status,
        String actualResult
) {}

