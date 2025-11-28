package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.TestCycleStatus;
import com.example.projectmanagement.enums.TestCycleType;

import java.time.LocalDateTime;

public record TestCycleSummaryResponse(
        Long id,
        String name,
        TestCycleType cycleType,
        TestCycleStatus status,
        Long projectId,
        Long sprintId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int runCount,          // how many runs under this cycle
        int completedRunCount  // for quick progress view (can be 0 for now)
) {}
