package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.testing.*;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.repository.StoryRepository;
import com.example.projectmanagement.entity.testing.TestStory;
import com.example.projectmanagement.repository.TestCaseRepository;
import com.example.projectmanagement.repository.TestScenarioRepository;
import com.example.projectmanagement.repository.TestStoryRepository;
import com.example.projectmanagement.service.TestStoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestStoryServiceImpl implements TestStoryService {

    private final TestStoryRepository testStoryRepository;
    private final ProjectRepository projectRepository;
    private final StoryRepository storyRepository;
    private final TestScenarioRepository testScenarioRepository;
    private final TestCaseRepository testCaseRepository;

    @Override
    @Transactional
    public TestStorySummaryResponse createTestStory(TestStoryCreateRequest request, Long currentUserId) {

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + request.projectId()));

        Story linkedStory = null;
        if (request.linkedStoryId() != null) {
            linkedStory = storyRepository.findById(request.linkedStoryId())
                    .orElseThrow(() -> new EntityNotFoundException("User Story not found: " + request.linkedStoryId()));
            // Optional: validate linkedStory.getProject().equals(project)
        }

        TestStory testStory = TestStory.builder()
                .project(project)
                .linkedUserStory(linkedStory)
                .name(request.name())
                .description(request.description())
                .createdBy(currentUserId)
                .createdAt(LocalDateTime.now())
                .build();

        TestStory saved = testStoryRepository.save(testStory);

        return new TestStorySummaryResponse(
                saved.getId(),
                saved.getName(),
                saved.getLinkedUserStory() != null ? saved.getLinkedUserStory().getId() : null,
                0,
                saved.getDescription()
        );
    }

    @Override
    public List<TestStorySummaryResponse> getTestStoriesForProject(Long projectId) {
        return testStoryRepository.findByProjectId(projectId).stream()
                .map(ts -> new TestStorySummaryResponse(
                        ts.getId(),
                        ts.getName(),
                        ts.getLinkedUserStory() != null ? ts.getLinkedUserStory().getId() : null,
                        testScenarioRepository.countByTestStoryId(ts.getId()),
                        ts.getDescription()
                ))
                .toList();
    }

    @Override
    public List<TestStorySummaryResponse> getTestStoriesForUserStory(Long storyId) {
        return testStoryRepository.findByLinkedUserStoryId(storyId).stream()
                .map(ts -> new TestStorySummaryResponse(
                        ts.getId(),
                        ts.getName(),
                        ts.getLinkedUserStory() != null ? ts.getLinkedUserStory().getId() : null,
                        testScenarioRepository.countByTestStoryId(ts.getId()),
                        ts.getDescription()
                ))
                .toList();
    }

    @Override
    public ProjectTestDataResponse getProjectTestData(Long projectId) {
        List<TestStory> stories = testStoryRepository.findByProjectId(projectId);

        List<TestStoryData> storyData = stories.stream()
                .map(story -> {
                    List<TestScenarioData> scenarios = testScenarioRepository.findByTestStoryId(story.getId()).stream()
                            .map(scenario -> {
                                List<TestCaseData> testCases = testCaseRepository.findByScenarioId(scenario.getId()).stream()
                                        .map(testCase -> new TestCaseData(
                                                testCase.getId(),
                                                testCase.getTitle(),
                                                testCase.getPreConditions(),
                                                testCase.getType().name(),
                                                testCase.getPriority().name(),
                                                testCase.getStatus()
                                        ))
                                        .collect(Collectors.toList());

                                return new TestScenarioData(
                                        scenario.getId(),
                                        scenario.getTitle(),
                                        scenario.getDescription(),
                                        testCases
                                );
                            })
                            .collect(Collectors.toList());

                    return new TestStoryData(
                            story.getId(),
                            story.getName(),
                            story.getDescription(),
                            scenarios
                    );
                })
                .collect(Collectors.toList());

        return new ProjectTestDataResponse(storyData);
    }
}
