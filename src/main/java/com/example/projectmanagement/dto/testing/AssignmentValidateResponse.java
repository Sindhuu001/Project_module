package com.example.projectmanagement.dto.testing;

import java.util.List;

public record AssignmentValidateResponse(
        boolean conflict,
        int totalCases,
        int alreadyInRunCount,
        int alreadyAssignedCount,
        int unassignedCount,
        List<AssignmentConflictItem> conflicts // list of conflicting cases (subset)
) {}
