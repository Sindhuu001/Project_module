package com.example.projectmanagement.dto.testing;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TestStepsReorderRequest(
        @NotEmpty List<Long> stepIdsInOrder
) {}
