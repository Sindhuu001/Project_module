package com.example.projectmanagement.dto.testing;

import java.util.List;

public record AssignmentApplyResponse(
        int createdCount,
        int updatedCount,
        List<Long> createdRunCaseIds
) {}

