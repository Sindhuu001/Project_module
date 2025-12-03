package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.AssignmentObjectType;
import jakarta.validation.constraints.NotNull;

public record AssignmentValidateRequest(
        @NotNull Long runId,
        @NotNull AssignmentObjectType objectType, // STORY | SCENARIO | CASE
        @NotNull Long objectId,
        Long assignTo // optional, if null => we only add cases to run without setting assignee
) {}

