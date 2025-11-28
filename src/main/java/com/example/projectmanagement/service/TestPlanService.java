package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.testing.TestPlanCreateRequest;
import com.example.projectmanagement.dto.testing.TestPlanSummaryResponse;

import java.util.List;

public interface TestPlanService {

    TestPlanSummaryResponse createPlan(TestPlanCreateRequest request, Long currentUserId);

    List<TestPlanSummaryResponse> getPlansForProject(Long projectId);

    TestPlanSummaryResponse getPlanDetail(Long planId);
}
