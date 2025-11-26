package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.testing.TestRunCreateRequest;
import com.example.projectmanagement.dto.testing.TestRunSummaryResponse;

import java.util.List;

public interface TestRunService {

    TestRunSummaryResponse createRun(TestRunCreateRequest request, Long currentUserId);

    List<TestRunSummaryResponse> getRunsForCycle(Long cycleId);

    TestRunSummaryResponse getRunDetail(Long runId);
}
