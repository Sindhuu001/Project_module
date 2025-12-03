package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.testing.TestScenarioCreateRequest;
import com.example.projectmanagement.dto.testing.TestScenarioSummaryResponse;

import java.util.List;

public interface TestScenarioService {

    TestScenarioSummaryResponse createScenario(TestScenarioCreateRequest request, Long currentUserId);

    List<TestScenarioSummaryResponse> getScenariosForPlan(Long planId);

    List<TestScenarioSummaryResponse> getScenariosForTestStory(Long testStoryId);

    List<TestScenarioSummaryResponse> getScenariosForUserStory(Long storyId);
}

