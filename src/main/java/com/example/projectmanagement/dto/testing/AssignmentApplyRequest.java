package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.AssignmentAction;
import com.example.projectmanagement.enums.AssignmentObjectType;
import jakarta.validation.constraints.NotNull;

public record AssignmentApplyRequest(
        @NotNull Long runId,
        @NotNull AssignmentObjectType objectType,
        @NotNull Long objectId,
        @NotNull AssignmentAction action, // REASSIGN_ALL | ASSIGN_UNASSIGNED | CANCEL
        Long assignTo // optional
) {}
