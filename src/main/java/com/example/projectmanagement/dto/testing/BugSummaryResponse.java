package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.BugPriority;

public record BugSummaryResponse(
        Long id,
        String title,
        BugPriority priority,
        StatusDto status,
        Long epicId
) {
    public record StatusDto(String id, String name) {}
}
