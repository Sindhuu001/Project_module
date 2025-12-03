package com.example.projectmanagement.dto.testing;


import com.example.projectmanagement.enums.TestCycleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TestCycleCreateRequest(
        @NotNull Long projectId,
        Long sprintId,                  // optional link to sprint
        @NotBlank String name,
        TestCycleType cycleType,        // can be null → default in service
        LocalDateTime startDate,        // optional, can be null
        LocalDateTime endDate           // optional, can be null
) {}

