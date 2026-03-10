package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.TestCycleStatus;
import com.example.projectmanagement.enums.TestCycleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TestCycleCreateRequest(
        @NotNull Long projectId,
        Long sprintId,
        @NotBlank String name,
        TestCycleType cycleType,
        TestCycleStatus status,      // used during updates
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
