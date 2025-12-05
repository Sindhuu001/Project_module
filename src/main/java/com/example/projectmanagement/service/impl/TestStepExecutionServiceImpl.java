package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.testing.AddAdHocStepRequest;
import com.example.projectmanagement.dto.testing.TestRunCaseStepResponse;
import com.example.projectmanagement.dto.testing.TestStepExecutionRequest;
import com.example.projectmanagement.entity.testing.TestRun;
import com.example.projectmanagement.entity.testing.TestRunCase;
import com.example.projectmanagement.entity.testing.TestRunCaseStep;
import com.example.projectmanagement.entity.testing.TestStep;
import com.example.projectmanagement.enums.TestRunCaseStatus;
import com.example.projectmanagement.enums.TestRunStatus;
import com.example.projectmanagement.enums.TestStepResultStatus;
import com.example.projectmanagement.repository.TestRunCaseRepository;
import com.example.projectmanagement.repository.TestRunCaseStepRepository;
import com.example.projectmanagement.repository.TestRunRepository;
import com.example.projectmanagement.repository.TestStepRepository;
import com.example.projectmanagement.service.BugService;
import com.example.projectmanagement.service.TestStepExecutionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestStepExecutionServiceImpl implements TestStepExecutionService {

    private static final Logger log = LoggerFactory.getLogger(TestStepExecutionServiceImpl.class);

    private final TestRunCaseRepository testRunCaseRepository;
    private final TestRunCaseStepRepository testRunCaseStepRepository;
    private final TestStepRepository testStepRepository;
    private final TestRunRepository testRunRepository;
    private final BugService bugService;

    @Override
    @Transactional
    public List<TestRunCaseStepResponse> getStepsForRunCase(Long runCaseId) {
        TestRunCase runCase = testRunCaseRepository.findById(runCaseId)
                .orElseThrow(() -> new EntityNotFoundException("TestRunCase not found: " + runCaseId));

        boolean initialized = testRunCaseStepRepository.existsByRunCaseId(runCaseId);
        if (!initialized && runCase.getTestCase() != null) { // Only init if it's from a blueprint
            initRunCaseSteps(runCase);
        }

        List<TestRunCaseStep> steps = testRunCaseStepRepository.findByRunCaseIdOrderByStepNumberAsc(runCaseId);
        return steps.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public TestRunCaseStepResponse executeStep(TestStepExecutionRequest request, Long currentUserId) {
        TestRunCase runCase = testRunCaseRepository.findById(request.runCaseId())
                .orElseThrow(() -> new EntityNotFoundException("TestRunCase not found: " + request.runCaseId()));

        TestRunCaseStep stepResult = testRunCaseStepRepository.findById(request.stepId())
                .orElseThrow(() -> new EntityNotFoundException("TestRunCaseStep not found: " + request.stepId()));

        stepResult.setStatus(request.status());
        stepResult.setActualResult(request.actualResult());
        stepResult.setExecutedBy(currentUserId);
        stepResult.setExecutedAt(LocalDateTime.now());
        stepResult.setUpdatedAt(LocalDateTime.now());

        TestRunCaseStep savedStep = testRunCaseStepRepository.save(stepResult);
        
        // Temporarily comment out to isolate the issue
        // recalcRunCaseStatus(runCase);
        // try {
        //     if (runCase.getStatus() == TestRunCaseStatus.FAILED) {
        //         bugService.handleCaseFailed(runCase.getId(), savedStep.getId(), currentUserId);
        //     } else if (runCase.getStatus() == TestRunCaseStatus.PASSED) {
        //         bugService.handleCasePassed(runCase.getId(), currentUserId);
        //     }
        // } catch (Exception ex) {
        //     log.error("Error in post-step bug handling for runCase {}: {}", runCase.getId(), ex.getMessage(), ex);
        // }

        return toDto(savedStep);
    }

    @Override
    @Transactional
    public TestRunCaseStepResponse addAdHocStep(Long runCaseId, AddAdHocStepRequest request, Long currentUserId) {
        TestRunCase runCase = testRunCaseRepository.findById(runCaseId)
                .orElseThrow(() -> new EntityNotFoundException("TestRunCase not found: " + runCaseId));

        int nextStepNumber = testRunCaseStepRepository.countByRunCaseId(runCaseId) + 1;

        TestRunCaseStep adHocStep = TestRunCaseStep.builder()
                .runCase(runCase)
                .step(null) // Explicitly null for ad-hoc steps
                .action(request.action())
                .expectedResult(request.expectedResult())
                .stepNumber(nextStepNumber)
                .status(TestStepResultStatus.NOT_EXECUTED)
                .createdBy(currentUserId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        TestRunCaseStep savedStep = testRunCaseStepRepository.save(adHocStep);
        return toDto(savedStep);
    }

    @Transactional
    protected void initRunCaseSteps(TestRunCase runCase) {
        Long testCaseId = runCase.getTestCase().getId();
        List<TestStep> designSteps = testStepRepository.findByTestCaseIdOrderByStepNumberAsc(testCaseId);
        LocalDateTime now = LocalDateTime.now();

        List<TestRunCaseStep> runSteps = designSteps.stream()
                .map(ds -> TestRunCaseStep.builder()
                        .runCase(runCase)
                        .step(ds)
                        .action(ds.getAction()) // Copy action
                        .expectedResult(ds.getExpectedResult()) // Copy expected result
                        .stepNumber(ds.getStepNumber())
                        .status(TestStepResultStatus.NOT_EXECUTED)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
                )
                .sorted(Comparator.comparing(TestRunCaseStep::getStepNumber))
                .toList();

        testRunCaseStepRepository.saveAll(runSteps);
    }

    @Transactional
    protected void recalcRunCaseStatus(TestRunCase runCase) {
        // ... existing implementation ...
    }

    @Transactional
    protected void updateRunStatus(TestRun run) {
        // ... existing implementation ...
    }

    private TestRunCaseStepResponse toDto(TestRunCaseStep step) {
        Long designStepId = (step.getStep() != null) ? step.getStep().getId() : null;
        return new TestRunCaseStepResponse(
                step.getId(),
                designStepId,
                step.getStepNumber(),
                step.getAction(),
                step.getExpectedResult(),
                step.getStatus().name(),
                step.getActualResult()
        );
    }
}
