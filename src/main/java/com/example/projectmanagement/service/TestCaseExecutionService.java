package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.testing.TestCaseExecutionRequest;
import com.example.projectmanagement.dto.testing.TestCaseExecutionResponse;

public interface TestCaseExecutionService {

    TestCaseExecutionResponse passCase(TestCaseExecutionRequest req, Long currentUserId);

    TestCaseExecutionResponse failCase(TestCaseExecutionRequest req, Long currentUserId);

    TestCaseExecutionResponse blockCase(TestCaseExecutionRequest req, Long currentUserId);

    TestCaseExecutionResponse skipCase(TestCaseExecutionRequest req, Long currentUserId);
}

