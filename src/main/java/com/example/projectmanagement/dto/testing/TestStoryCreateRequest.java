package com.example.projectmanagement.dto.testing;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TestStoryCreateRequest(
        @NotNull Long projectId,
        Long linkedStoryId,          // can be null
        @NotBlank String name,
        String description
) {}
