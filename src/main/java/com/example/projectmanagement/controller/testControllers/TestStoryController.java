package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.ProjectTestDataResponse;
import com.example.projectmanagement.dto.testing.TestStoryCreateRequest;
import com.example.projectmanagement.dto.testing.TestStorySummaryResponse;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.TestStoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/test-design/test-stories")
@RequiredArgsConstructor
public class TestStoryController {

    private final TestStoryService testStoryService;

    @PostMapping
    public ResponseEntity<TestStorySummaryResponse> createTestStory(
            @Valid @RequestBody TestStoryCreateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        Long currentUserId = currentUser.getId();
        TestStorySummaryResponse response = testStoryService.createTestStory(request, currentUserId);
        return ResponseEntity.ok(response);
    }

    // Test stories for project
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<TestStorySummaryResponse>> getTestStoriesForProject(
            @PathVariable Long projectId
    ) {
        List<TestStorySummaryResponse> stories = testStoryService.getTestStoriesForProject(projectId);
        return ResponseEntity.ok(stories);
    }

    // Test stories for a user story
    @GetMapping("/stories/{storyId}")
    public ResponseEntity<List<TestStorySummaryResponse>> getTestStoriesForUserStory(
            @PathVariable Long storyId
    ) {
        List<TestStorySummaryResponse> stories = testStoryService.getTestStoriesForUserStory(storyId);
        return ResponseEntity.ok(stories);
    }

    @GetMapping("/project-test-data/{projectId}")
    public ResponseEntity<ProjectTestDataResponse> getProjectTestData(@PathVariable Long projectId) {
        return ResponseEntity.ok(testStoryService.getProjectTestData(projectId));
    }


}
