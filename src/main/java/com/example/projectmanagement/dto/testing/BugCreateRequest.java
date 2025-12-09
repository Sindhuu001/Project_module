package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.BugPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BugCreateRequest(
        @NotNull Long runCaseId,
        Long runCaseStepId,          // optional — failing step
        @NotBlank String title,
        String description,
        String reproductionSteps,
        String expected,
        String actual,
        @NotNull String severity,    // use enum names
        BugPriority priority,             // optional
        Long assignedTo,              // optional developer id
        @NotBlank String type
) {}
