package com.example.projectmanagement.service.impl;

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
import com.example.projectmanagement.service.TestStepExecutionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestStepExecutionServiceImpl implements TestStepExecutionService {

    private final TestRunCaseRepository testRunCaseRepository;
    private final TestRunCaseStepRepository testRunCaseStepRepository;
    private final TestStepRepository testStepRepository;
    private final TestRunRepository testRunRepository;

    @Override
    @Transactional
    public List<TestRunCaseStepResponse> getStepsForRunCase(Long runCaseId) {
        TestRunCase runCase = testRunCaseRepository.findById(runCaseId)
                .orElseThrow(() -> new EntityNotFoundException("TestRunCase not found: " + runCaseId));

        // If no run-case steps exist yet → initialize from design-time steps
        boolean initialized = testRunCaseStepRepository.existsByRunCaseId(runCaseId);
        if (!initialized) {
            initRunCaseSteps(runCase);
        }

        List<TestRunCaseStep> steps = testRunCaseStepRepository
                .findByRunCaseIdOrderByStepNumberAsc(runCaseId);

        return steps.stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public TestRunCaseStepResponse executeStep(TestStepExecutionRequest request, Long currentUserId) {
        TestRunCase runCase = testRunCaseRepository.findById(request.runCaseId())
                .orElseThrow(() -> new EntityNotFoundException("TestRunCase not found: " + request.runCaseId()));

        // Ensure steps are initialized for this run-case
        boolean initialized = testRunCaseStepRepository.existsByRunCaseId(runCase.getId());
        if (!initialized) {
            initRunCaseSteps(runCase);
        }

        TestRunCaseStep stepResult = testRunCaseStepRepository
                .findByRunCaseIdAndStepId(runCase.getId(), request.stepId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Step " + request.stepId() + " not found for runCase " + runCase.getId())
                );

        stepResult.setStatus(request.status());
        stepResult.setActualResult(request.actualResult());
        stepResult.setExecutedBy(currentUserId);
        stepResult.setExecutedAt(LocalDateTime.now());
        stepResult.setUpdatedAt(LocalDateTime.now());

        TestRunCaseStep savedStep = testRunCaseStepRepository.save(stepResult);

        // Update run-case and run statuses based on all step results
        recalcRunCaseStatus(runCase);

        return toDto(savedStep);
    }

    // Initialize TestRunCaseStep rows from design-time TestStep list
    @Transactional
    protected void initRunCaseSteps(TestRunCase runCase) {
        Long testCaseId = runCase.getTestCase().getId();

        List<TestStep> designSteps = testStepRepository.findByTestCaseIdOrderByStepNumberAsc(testCaseId);
        LocalDateTime now = LocalDateTime.now();

        List<TestRunCaseStep> runSteps = designSteps.stream()
                .map(ds -> TestRunCaseStep.builder()
                        .runCase(runCase)
                        .step(ds)
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
        List<TestRunCaseStep> steps = testRunCaseStepRepository
                .findByRunCaseIdOrderByStepNumberAsc(runCase.getId());

        if (steps.isEmpty()) {
            // No steps: we can consider whole case as single unit
            return;
        }

        boolean anyFailed = steps.stream().anyMatch(s -> s.getStatus() == TestStepResultStatus.FAILED);
        boolean anyBlocked = steps.stream().anyMatch(s -> s.getStatus() == TestStepResultStatus.BLOCKED);
        boolean anyExecuted = steps.stream().anyMatch(s -> s.getStatus() != TestStepResultStatus.NOT_EXECUTED);
        boolean allPassedOrSkipped = steps.stream().allMatch(s ->
                s.getStatus() == TestStepResultStatus.PASSED ||
                        s.getStatus() == TestStepResultStatus.SKIPPED
        );
        boolean allNotExecuted = steps.stream().allMatch(s -> s.getStatus() == TestStepResultStatus.NOT_EXECUTED);

        TestRunCaseStatus newStatus;

        if (allNotExecuted) {
            newStatus = TestRunCaseStatus.NOT_STARTED;
        } else if (anyFailed) {
            newStatus = TestRunCaseStatus.FAILED;
        } else if (anyBlocked) {
            newStatus = TestRunCaseStatus.BLOCKED;
        } else if (allPassedOrSkipped) {
            newStatus = TestRunCaseStatus.PASSED;
        } else if (anyExecuted) {
            newStatus = TestRunCaseStatus.IN_PROGRESS;
        } else {
            newStatus = runCase.getStatus(); // fallback
        }

        runCase.setStatus(newStatus);
        runCase.setLastExecutedAt(LocalDateTime.now());
        testRunCaseRepository.save(runCase);

        // Also update TestRun's status if needed
        updateRunStatus(runCase.getRun());
    }

    @Transactional
    protected void updateRunStatus(TestRun run) {
        // load all cases for this run
        var runCases = testRunCaseRepository.findByRunId(run.getId());

        boolean anyInProgress = runCases.stream()
                .anyMatch(rc -> rc.getStatus() == TestRunCaseStatus.IN_PROGRESS);
        boolean anyNotStarted = runCases.stream()
                .anyMatch(rc -> rc.getStatus() == TestRunCaseStatus.NOT_STARTED);
        boolean anyFailed = runCases.stream()
                .anyMatch(rc -> rc.getStatus() == TestRunCaseStatus.FAILED ||
                        rc.getStatus() == TestRunCaseStatus.BLOCKED);
        boolean allCompleted = !runCases.isEmpty() && runCases.stream()
                .allMatch(rc -> rc.getStatus() == TestRunCaseStatus.PASSED ||
                        rc.getStatus() == TestRunCaseStatus.FAILED ||
                        rc.getStatus() == TestRunCaseStatus.BLOCKED);

        TestRunStatus newStatus;

        if (allCompleted) {
            newStatus = TestRunStatus.COMPLETED;
        } else if (anyInProgress || (!anyNotStarted && !runCases.isEmpty())) {
            newStatus = TestRunStatus.IN_PROGRESS;
        } else {
            // default if all NOT_STARTED
            newStatus = TestRunStatus.CREATED;
        }

        if (run.getStatus() != newStatus) {
            run.setStatus(newStatus);
            testRunRepository.save(run);
        }
    }

    private TestRunCaseStepResponse toDto(TestRunCaseStep step) {
        TestStep designStep = step.getStep();
        return new TestRunCaseStepResponse(
                step.getId(),
                designStep.getId(),
                step.getStepNumber(),
                designStep.getAction(),
                designStep.getExpectedResult(),
                step.getStatus().name(),
                step.getActualResult()
        );
    }
}
