package com.example.projectmanagement.dto.testing;
import jakarta.validation.constraints.NotBlank;

public record TestStepCreateRequest(
        @NotBlank String action,
        @NotBlank String expectedResult
) {}
