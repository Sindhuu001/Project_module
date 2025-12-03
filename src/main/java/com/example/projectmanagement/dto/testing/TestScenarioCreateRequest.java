package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.TestPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TestScenarioCreateRequest(
        @NotNull Long testPlanId,
        Long testStoryId,
        Long linkedStoryId,
        @NotBlank String title,
        String description,
        TestPriority priority
) {}

