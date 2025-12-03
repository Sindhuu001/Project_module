package com.example.projectmanagement.service;
import com.example.projectmanagement.dto.testing.TestStepCreateRequest;
import com.example.projectmanagement.dto.testing.TestStepResponse;
import com.example.projectmanagement.dto.testing.TestStepsReorderRequest;

import java.util.List;

public interface TestStepService {

    List<TestStepResponse> getStepsForCase(Long caseId);

    List<TestStepResponse> addStepsToCase(Long caseId, List<TestStepCreateRequest> steps);

    TestStepResponse updateStep(Long stepId, TestStepCreateRequest request);

    List<TestStepResponse> reorderSteps(Long caseId, TestStepsReorderRequest request);
}
