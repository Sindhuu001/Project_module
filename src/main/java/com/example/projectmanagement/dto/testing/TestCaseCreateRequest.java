package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.TestCaseType;
import com.example.projectmanagement.enums.TestPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TestCaseCreateRequest(
        @NotNull Long scenarioId,
        @NotBlank String title,
        String preConditions,
        TestCaseType type,
        TestPriority priority,
        List<TestStepCreateRequest> steps // can be null or empty
) {}

