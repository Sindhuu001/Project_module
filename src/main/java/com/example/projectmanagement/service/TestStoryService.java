package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.testing.*;

import java.util.List;

public interface TestStoryService {

    TestStorySummaryResponse createTestStory(TestStoryCreateRequest request, Long currentUserId);

    List<TestStorySummaryResponse> getTestStoriesForProject(Long projectId);

    List<TestStorySummaryResponse> getTestStoriesForUserStory(Long storyId);

    ProjectTestDataResponse getProjectTestData(Long projectId);

    TestStorySummaryResponse updateTestStory(Long testStoryId, TestStoryUpdateRequest request, Long currentUserId);

    void deleteTestStory(Long testStoryId);
}
