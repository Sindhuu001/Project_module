package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.TestCaseType;
import com.example.projectmanagement.enums.TestPriority;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record TestCaseUpdateRequest(
        @NotBlank(message = "Title is required") String title,
        String preConditions,
        TestCaseType type,
        TestPriority priority,
        List<TestStepCreateRequest> steps // Used to fully replace existing steps
) {}