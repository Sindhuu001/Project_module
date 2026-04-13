package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.testing.TestPlan;
import com.example.projectmanagement.entity.testing.TestScenario;
import com.example.projectmanagement.entity.testing.TestStory;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.TestCaseRepository;
import com.example.projectmanagement.repository.TestPlanRepository;
import com.example.projectmanagement.repository.TestScenarioRepository;
import com.example.projectmanagement.repository.TestStoryRepository;
import com.example.projectmanagement.repository.StoryRepository;
import com.example.projectmanagement.dto.testing.TestScenarioCreateRequest;
import com.example.projectmanagement.dto.testing.TestScenarioSummaryResponse;
import com.example.projectmanagement.enums.TestPriority;
import com.example.projectmanagement.enums.TestScenarioStatus;
import com.example.projectmanagement.service.TestScenarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.projectmanagement.dto.testing.TestScenarioUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestScenarioServiceImpl implements TestScenarioService {

    private final TestScenarioRepository testScenarioRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestPlanRepository testPlanRepository;
    private final TestStoryRepository testStoryRepository;
    private final StoryRepository storyRepository;
    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public TestScenarioSummaryResponse createScenario(TestScenarioCreateRequest request, Long currentUserId) {

        TestPlan plan = testPlanRepository.findById(request.testPlanId())
                .orElseThrow(() -> new EntityNotFoundException("Test Plan not found: " + request.testPlanId()));

        Project project = plan.getProject();

        TestStory testStory = null;
        if (request.testStoryId() != null) {
            testStory = testStoryRepository.findById(request.testStoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Test Story not found: " + request.testStoryId()));

            if (!testStory.getProject().getId().equals(project.getId())) {
                throw new IllegalArgumentException("Test Story does not belong to same Project");
            }
        }

        Story userStory = null;
        if (request.linkedStoryId() != null) {
            userStory = storyRepository.findById(request.linkedStoryId())
                    .orElseThrow(() -> new EntityNotFoundException("User Story not found: " + request.linkedStoryId()));

            if (!userStory.getProject().getId().equals(project.getId())) {
                throw new IllegalArgumentException("User Story does not belong to same Project");
            }
        }

        // Consistency validation: if testStory has story link → must match
        if (testStory != null && testStory.getLinkedUserStory() != null && userStory != null &&
                !testStory.getLinkedUserStory().getId().equals(userStory.getId())) {
            throw new IllegalArgumentException("Mismatch: Scenario links to a different User Story than its Test Story");
        }

        TestScenario scenario = TestScenario.builder()
                .testPlan(plan)
                .testStory(testStory)
                .linkedUserStory(userStory)
                .title(request.title())
                .description(request.description())
                .priority(request.priority() != null ? request.priority() : TestPriority.MEDIUM)
                .status(TestScenarioStatus.DRAFT)
                .createdBy(currentUserId)
                .updatedAt(LocalDateTime.now())
                .build();

        TestScenario saved = testScenarioRepository.save(scenario);

        return toDto(saved, 0);
    }

    @Override
    public List<TestScenarioSummaryResponse> getScenariosForPlan(Long planId) {
        return listToDtos(testScenarioRepository.findByTestPlanId(planId));
    }

    @Override
    public List<TestScenarioSummaryResponse> getScenariosForTestStory(Long testStoryId) {
        return listToDtos(testScenarioRepository.findByTestStoryId(testStoryId));
    }

    @Override
    public List<TestScenarioSummaryResponse> getScenariosForUserStory(Long storyId) {
        return listToDtos(testScenarioRepository.findByLinkedUserStoryId(storyId));
    }

    private List<TestScenarioSummaryResponse> listToDtos(List<TestScenario> scenarios) {
        return scenarios.stream().map(s ->
                toDto(s, testCaseRepository.countByScenarioId(s.getId()))
        ).toList();
    }

    private TestScenarioSummaryResponse toDto(TestScenario scenario, int caseCount) {
        return new TestScenarioSummaryResponse(
                scenario.getId(),
                scenario.getTitle(),
                scenario.getPriority().name(),
                scenario.getStatus().name(),
                scenario.getTestStory() != null ? scenario.getTestStory().getId() : null,
                scenario.getLinkedUserStory() != null ? scenario.getLinkedUserStory().getId() : null,
                caseCount
        );
    }
    @Override
    @Transactional
    public TestScenarioSummaryResponse updateScenario(Long id, TestScenarioUpdateRequest request, Long currentUserId) {
        TestScenario scenario = testScenarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test Scenario not found: " + id));

        // Update fields if they are provided in the request
        if (request.title() != null) {
            scenario.setTitle(request.title());
        }
        if (request.description() != null) {
            scenario.setDescription(request.description());
        }
        if (request.priority() != null) {
            scenario.setPriority(request.priority());
        }
        if (request.status() != null) {
            scenario.setStatus(request.status());
        }

        // Update auditing fields
        // scenario.setUpdatedBy(currentUserId); // Uncomment if you have this field in your entity
        scenario.setUpdatedAt(LocalDateTime.now());

        // Because of JPA dirty checking, the entity will be updated automatically upon transaction commit.
        // We just need to fetch the count and return the mapped DTO.
        int caseCount = testCaseRepository.countByScenarioId(scenario.getId());
        
        return toDto(scenario, caseCount);
    }

    @Override
    @Transactional
    public void deleteScenario(Long id, Long currentUserId) {
        TestScenario scenario = testScenarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test Scenario not found: " + id));

        // Optional safety check: Ensure you don't orphan or accidentally delete child test cases
        int caseCount = testCaseRepository.countByScenarioId(id);
        if (caseCount > 0) {
            throw new IllegalStateException("Cannot delete Test Scenario because it contains " + caseCount + " active test cases. Delete them first.");
        }

        testScenarioRepository.delete(scenario);
    }
}
