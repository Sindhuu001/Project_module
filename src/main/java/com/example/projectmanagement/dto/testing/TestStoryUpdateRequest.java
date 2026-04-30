package com.example.projectmanagement.dto.testing;

import jakarta.validation.constraints.NotBlank;

public record TestStoryUpdateRequest(
        @NotBlank String name,
        String description,
        Long linkedStoryId
) {}
