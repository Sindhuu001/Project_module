package com.example.projectmanagement.dto.testing;

import jakarta.validation.constraints.NotNull;

public record BugAssignRequest(
        @NotNull Long assigneeId
) {}
