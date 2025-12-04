package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.testing.AddAdHocStepRequest;
import com.example.projectmanagement.dto.testing.TestRunCaseStepResponse;
import com.example.projectmanagement.dto.testing.TestStepExecutionRequest;

import java.util.List;

public interface TestStepExecutionService {

    List<TestRunCaseStepResponse> getStepsForRunCase(Long runCaseId);

    TestRunCaseStepResponse executeStep(TestStepExecutionRequest request, Long currentUserId);

    TestRunCaseStepResponse addAdHocStep(Long runCaseId, AddAdHocStepRequest request, Long currentUserId);
}
