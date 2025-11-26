package com.example.projectmanagement.dto.testing;

import java.util.List;

public record TestCaseExecutionResponse(
        Long runCaseId,
        String caseStatus,
        List<TestRunCaseStepResponse> steps
) {}

