package com.example.projectmanagement.dto.testing;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record TestRunCreateRequest(
        @NotNull Long cycleId,
        String name,        // optional custom name
        String description, // optional
        List<Long> testCaseIds // List of test case IDs to include in the run
) {}
