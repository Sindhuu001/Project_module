package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.dto.testing.TestPlanCreateRequest;
import com.example.projectmanagement.dto.testing.TestPlanSummaryResponse;
import com.example.projectmanagement.entity.testing.TestPlan;
import com.example.projectmanagement.repository.TestCaseRepository;
import com.example.projectmanagement.repository.TestPlanRepository;
import com.example.projectmanagement.repository.TestScenarioRepository;
import com.example.projectmanagement.service.TestPlanService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestPlanServiceImpl implements TestPlanService {

    private final TestPlanRepository testPlanRepository;
    private final ProjectRepository projectRepository;
    private final TestScenarioRepository testScenarioRepository;
    private final TestCaseRepository testCaseRepository;

    @Override
    @Transactional
    public TestPlanSummaryResponse createPlan(TestPlanCreateRequest request, Long currentUserId) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + request.projectId()));

        // TODO: optional uniqueness check on name within project

        TestPlan plan = TestPlan.builder()
                .project(project)
                .name(request.name())
                .objective(request.objective())
                .createdBy(currentUserId)
                .createdAt(LocalDateTime.now())
                .build();

        TestPlan saved = testPlanRepository.save(plan);

        return new TestPlanSummaryResponse(
                saved.getId(),
                saved.getName(),
                saved.getObjective(),
                saved.getCreatedAt(),
                0,
                0,
                0
        );
    }

    @Override
    public List<TestPlanSummaryResponse> getPlansForProject(Long projectId) {
        // Lightweight list – counts optional here, can be 0 or precomputed later
        return testPlanRepository.findByProjectId(projectId).stream()
                .map(plan -> new TestPlanSummaryResponse(
                        plan.getId(),
                        plan.getName(),
                        plan.getObjective(),
                        plan.getCreatedAt(),
                        0,
                        0,
                        0
                ))
                .toList();
    }

    @Override
    public TestPlanSummaryResponse getPlanDetail(Long planId) {
        TestPlan plan = testPlanRepository.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Test Plan not found: " + planId));

        int scenarioCount = testScenarioRepository.countByTestPlanId(planId);
        int caseCount = testCaseRepository.countByScenarioTestPlanId(planId);
        int coveredStoryCount = testScenarioRepository.countDistinctStoriesByTestPlanId(planId);

        return new TestPlanSummaryResponse(
                plan.getId(),
                plan.getName(),
                plan.getObjective(),
                plan.getCreatedAt(),
                scenarioCount,
                caseCount,
                coveredStoryCount
        );
    }
}
