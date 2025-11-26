package com.example.projectmanagement.dto.testing;

import java.util.List;

public record BulkExecutionRequest(
        List<Long> runCaseIds
) {}
