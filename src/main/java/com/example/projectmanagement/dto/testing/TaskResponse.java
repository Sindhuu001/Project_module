package com.example.projectmanagement.dto.testing;

public record TaskResponse(
        Long id,
        String title,
        Long sprintId,
        String sprintName,
        String status
) {}

