package com.example.projectmanagement.dto.testing;

import java.util.List;

public record BulkAssignRequest(
        Long assigneeId,
        List<Long> runCaseIds
) {}

