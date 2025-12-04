package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.testing.AddCasesToRunRequest;
import com.example.projectmanagement.dto.testing.TestRunCreateRequest;
import com.example.projectmanagement.dto.testing.TestRunSummaryResponse;

import java.util.List;

public interface TestRunService {

    TestRunSummaryResponse createRun(TestRunCreateRequest request, Long currentUserId);

    void addTestCasesToRun(Long runId, AddCasesToRunRequest request);

    List<TestRunSummaryResponse> getRunsForCycle(Long cycleId);

    TestRunSummaryResponse getRunDetail(Long runId);
}
