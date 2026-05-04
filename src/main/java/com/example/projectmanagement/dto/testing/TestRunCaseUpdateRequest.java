package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.TestRunCaseStatus;

public record TestRunCaseUpdateRequest(
        TestRunCaseStatus status,
        Long assigneeId
) {}