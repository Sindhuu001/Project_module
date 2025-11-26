package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.testing.TestCaseCreateRequest;
import com.example.projectmanagement.dto.testing.TestCaseDetailResponse;
import com.example.projectmanagement.dto.testing.TestCaseSummaryResponse;
import com.example.projectmanagement.dto.testing.TestStepCreateRequest;
import com.example.projectmanagement.dto.testing.TestStepResponse;
import com.example.projectmanagement.entity.testing.TestCase;
import com.example.projectmanagement.entity.testing.TestScenario;
import com.example.projectmanagement.entity.testing.TestStep;
import com.example.projectmanagement.enums.TestCaseType;
import com.example.projectmanagement.enums.TestPriority;
import com.example.projectmanagement.repository.TestCaseRepository;
import com.example.projectmanagement.repository.TestScenarioRepository;
import com.example.projectmanagement.repository.TestStepRepository;
import com.example.projectmanagement.service.TestCaseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestScenarioRepository testScenarioRepository;
    private final TestStepRepository testStepRepository;

    @Override
    @Transactional
    public TestCaseSummaryResponse createTestCase(TestCaseCreateRequest request, Long currentUserId) {

        TestScenario scenario = testScenarioRepository.findById(request.scenarioId())
                .orElseThrow(() -> new EntityNotFoundException("Test Scenario not found: " + request.scenarioId()));

        TestCaseType type = request.type() != null ? request.type() : TestCaseType.FUNCTIONAL;
        TestPriority priority = request.priority() != null ? request.priority() : TestPriority.MEDIUM;

        TestCase testCase = TestCase.builder()
                .scenario(scenario)
                .title(request.title())
                .preConditions(request.preConditions())
                .type(type)
                .priority(priority)
                .status("READY") // as per your decision
                .createdBy(currentUserId)
                .updatedAt(LocalDateTime.now())
                .build();

        TestCase savedCase = testCaseRepository.save(testCase);

        int stepCount = 0;
        List<TestStepCreateRequest> stepsInput = request.steps();
        if (stepsInput != null && !stepsInput.isEmpty()) {
            List<TestStep> steps = new ArrayList<>(stepsInput.size());
            int stepNumber = 1;
            for (TestStepCreateRequest stepReq : stepsInput) {
                TestStep step = TestStep.builder()
                        .testCase(savedCase)
                        .stepNumber(stepNumber++)
                        .action(stepReq.action())
                        .expectedResult(stepReq.expectedResult())
                        .build();
                steps.add(step);
            }
            testStepRepository.saveAll(steps); // batch insert for speed
            stepCount = steps.size();
        }

        return new TestCaseSummaryResponse(
                savedCase.getId(),
                savedCase.getTitle(),
                savedCase.getType().name(),
                savedCase.getPriority().name(),
                savedCase.getStatus(),
                stepCount
        );
    }

    @Override
    public List<TestCaseSummaryResponse> getCasesForScenario(Long scenarioId) {
        // One query to fetch all test cases, separate count for steps per case
        List<TestCase> cases = testCaseRepository.findByScenarioId(scenarioId);
        return cases.stream()
                .map(tc -> {
                    int stepCount = testStepRepository.countByTestCaseId(tc.getId());
                    return new TestCaseSummaryResponse(
                            tc.getId(),
                            tc.getTitle(),
                            tc.getType().name(),
                            tc.getPriority().name(),
                            tc.getStatus(),
                            stepCount
                    );
                })
                .toList();
    }

    @Override
    public TestCaseDetailResponse getCaseDetail(Long caseId) {
        TestCase testCase = testCaseRepository.findById(caseId)
                .orElseThrow(() -> new EntityNotFoundException("Test Case not found: " + caseId));

        List<TestStep> steps = testStepRepository.findByTestCaseIdOrderByStepNumberAsc(caseId);

        List<TestStepResponse> stepDtos = steps.stream()
                .map(s -> new TestStepResponse(
                        s.getId(),
                        s.getStepNumber(),
                        s.getAction(),
                        s.getExpectedResult()
                ))
                .toList();

        return new TestCaseDetailResponse(
                testCase.getId(),
                testCase.getTitle(),
                testCase.getPreConditions(),
                testCase.getType().name(),
                testCase.getPriority().name(),
                testCase.getStatus(),
                stepDtos
        );
    }
}
