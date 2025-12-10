package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.testing.TestCaseCreateRequest;
import com.example.projectmanagement.dto.testing.TestCaseDetailResponse;
import com.example.projectmanagement.dto.testing.TestCaseSummaryResponse;

import java.util.List;

public interface TestCaseService {

    TestCaseSummaryResponse createTestCase(TestCaseCreateRequest request, Long currentUserId);

    List<TestCaseSummaryResponse> getCasesForScenario(Long scenarioId);

    TestCaseDetailResponse getCaseDetail(Long caseId);
    // List<TestCaseSummaryResponse> getCasesForProject(Long projectId);

    List<TestCaseSummaryResponse> getCasesForProject(Long projectId);
}

