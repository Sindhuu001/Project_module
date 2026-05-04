package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.ProjectTestDataResponse;
import com.example.projectmanagement.dto.testing.TestStoryCreateRequest;
import com.example.projectmanagement.dto.testing.TestStoryUpdateRequest;
import com.example.projectmanagement.dto.testing.TestStorySummaryResponse;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.TestStoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;


@RestController
@RequestMapping("/api/test-design/test-stories")
@RequiredArgsConstructor
public class TestStoryController {

    private final TestStoryService testStoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<TestStorySummaryResponse> createTestStory(
            @Valid @RequestBody TestStoryCreateRequest request,
            @CurrentUser UserDto currentUser) {
        Long currentUserId = currentUser.getId();
        TestStorySummaryResponse response = testStoryService.createTestStory(request, currentUserId);
        return ResponseEntity.ok(response);
    }

    // Test stories for project
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<TestStorySummaryResponse>> getTestStoriesForProject(
            @PathVariable Long projectId) {
        List<TestStorySummaryResponse> stories = testStoryService.getTestStoriesForProject(projectId);
        return ResponseEntity.ok(stories);
    }

    // Test stories for a user story
    @GetMapping("/stories/{storyId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<TestStorySummaryResponse>> getTestStoriesForUserStory(
            @PathVariable Long storyId) {
        List<TestStorySummaryResponse> stories = testStoryService.getTestStoriesForUserStory(storyId);
        return ResponseEntity.ok(stories);
    }

    @GetMapping("/project-test-data/{projectId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<ProjectTestDataResponse> getProjectTestData(@PathVariable Long projectId) {
        return ResponseEntity.ok(testStoryService.getProjectTestData(projectId));
    }

    @PutMapping("/project-test-data/{testStoryId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<TestStorySummaryResponse> updateTestStory(
            @PathVariable Long testStoryId,
            @Valid @RequestBody TestStoryUpdateRequest request,
            @CurrentUser UserDto currentUser) {
        TestStorySummaryResponse response = testStoryService.updateTestStory(testStoryId, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/project-test-data/{testStoryId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<Void> deleteTestStory(@PathVariable Long testStoryId) {
        testStoryService.deleteTestStory(testStoryId);
        return ResponseEntity.noContent().build();
    }
}
