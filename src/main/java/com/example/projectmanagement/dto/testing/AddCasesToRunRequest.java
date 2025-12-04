package com.example.projectmanagement.dto.testing;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AddCasesToRunRequest(
        @NotEmpty(message = "testCaseIds cannot be empty")
        List<Long> testCaseIds
) {}
