package com.example.projectmanagement.dto.testing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BugStatusUpdateRequest(
        @NotNull String status,  // NEW, IN_PROGRESS, FIXED, REOPENED, CLOSED
        String comment
) {}
