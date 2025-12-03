package com.example.projectmanagement.service;
import com.example.projectmanagement.dto.testing.TestCycleCreateRequest;
import com.example.projectmanagement.dto.testing.TestCycleSummaryResponse;

import java.util.List;

public interface TestCycleService {

    TestCycleSummaryResponse createCycle(TestCycleCreateRequest request, Long currentUserId);

    List<TestCycleSummaryResponse> getCyclesForProject(Long projectId);

    TestCycleSummaryResponse getCycleDetail(Long cycleId);
}
