package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.testing.TestRunCaseStepResponse;
import com.example.projectmanagement.dto.testing.TestStepExecutionRequest;

import java.util.List;

public interface TestStepExecutionService {

    // Load steps for a run-case, initialize run-case step rows if not present
    List<TestRunCaseStepResponse> getStepsForRunCase(Long runCaseId);

    // Execute a single step (set status + actualResult)
    TestRunCaseStepResponse executeStep(TestStepExecutionRequest request, Long currentUserId);
}

