package com.example.projectmanagement.dto.testing;

import jakarta.validation.constraints.NotNull;
import java.util.List;

import com.example.projectmanagement.enums.TestRunStatus;

public record TestRunCreateRequest(
        @NotNull Long cycleId,
        String name,        // optional custom name
        String description, // optional
        TestRunStatus status,
        Long executedBy,
        List<Long> testCaseIds // List of test case IDs to include in the run
) {

    }
