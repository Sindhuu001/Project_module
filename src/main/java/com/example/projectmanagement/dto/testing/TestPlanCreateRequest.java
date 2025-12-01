package com.example.projectmanagement.dto.testing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TestPlanCreateRequest(
        @NotNull Long projectId,
        @NotBlank String name,
        String objective
) {}
