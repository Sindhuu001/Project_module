package com.example.projectmanagement.dto.testing;

import jakarta.validation.constraints.NotNull;

public record TestRunCreateRequest(
        @NotNull Long cycleId,
        String name,        // optional custom name
        String description  // optional
) {}
