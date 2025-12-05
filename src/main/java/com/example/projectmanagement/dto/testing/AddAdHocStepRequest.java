package com.example.projectmanagement.dto.testing;

import jakarta.validation.constraints.NotEmpty;

public record AddAdHocStepRequest(
        @NotEmpty(message = "Action cannot be empty")
        String action,
        String expectedResult
) {}
