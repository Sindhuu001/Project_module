package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.testing.TestStoryCreateRequest;
import com.example.projectmanagement.dto.testing.TestStorySummaryResponse;

import java.util.List;

public interface TestStoryService {

    TestStorySummaryResponse createTestStory(TestStoryCreateRequest request, Long currentUserId);

    List<TestStorySummaryResponse> getTestStoriesForProject(Long projectId);

    List<TestStorySummaryResponse> getTestStoriesForUserStory(Long storyId);
}
