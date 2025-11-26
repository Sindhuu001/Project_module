package com.example.projectmanagement.dto.testing;

import java.util.List;

public record TestCaseDetailResponse(
        Long id,
        String title,
        String preConditions,
        String type,
        String priority,
        String status,
        List<TestStepResponse> steps
) {}