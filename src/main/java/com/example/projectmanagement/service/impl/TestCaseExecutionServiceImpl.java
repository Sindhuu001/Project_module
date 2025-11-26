package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.testing.TestCaseExecutionRequest;
import com.example.projectmanagement.dto.testing.TestCaseExecutionResponse;
import com.example.projectmanagement.dto.testing.TestRunCaseStepResponse;
import com.example.projectmanagement.entity.testing.TestRun;
import com.example.projectmanagement.entity.testing.TestRunCase;
import com.example.projectmanagement.entity.testing.TestRunCaseStep;
import com.example.projectmanagement.enums.TestRunCaseStatus;
import com.example.projectmanagement.enums.TestRunStatus;
import com.example.projectmanagement.enums.TestStepResultStatus;
import com.example.projectmanagement.repository.TestRunCaseRepository;
import com.example.projectmanagement.repository.TestRunCaseStepRepository;
import com.example.projectmanagement.repository.TestRunRepository;
import com.example.projectmanagement.service.BugService;
import com.example.projectmanagement.service.TestCaseExecutionService;
import com.example.projectmanagement.service.TestStepExecutionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TestCaseExecutionServiceImpl implements TestCaseExecutionService {

    private final TestRunCaseRepository testRunCaseRepository;
    private final TestRunCaseStepRepository testRunCaseStepRepository;
    private final TestRunRepository testRunRepository;
    private final TestStepExecutionService testStepExecutionService; // reuse init logic
    private final BugService bugService;
    // --- Pass entire case: mark all steps PASSED ---
    @Override
    @Transactional
    public TestCaseExecutionResponse passCase(TestCaseExecutionRequest req, Long currentUserId) {
        TestRunCase runCase = findRunCase(req.runCaseId());

        // ensure steps exist and load them (this also initializes them if needed)
        List<TestRunCaseStepResponse> stepDtos = testStepExecutionService.getStepsForRunCase(runCase.getId());

        // fetch actual step entities for update
        List<TestRunCaseStep> steps = testRunCaseStepRepository.findByRunCaseIdOrderByStepNumberAsc(runCase.getId());

        LocalDateTime now = LocalDateTime.now();
        for (TestRunCaseStep s : steps) {
            s.setStatus(TestStepResultStatus.PASSED);
            s.setActualResult(req.actualResult());
            s.setExecutedBy(currentUserId);
            s.setExecutedAt(now);
            s.setUpdatedAt(now);
        }

        testRunCaseStepRepository.saveAll(steps);

        // update runCase status
        runCase.setStatus(TestRunCaseStatus.PASSED);
        runCase.setLastExecutedAt(now);
        testRunCaseRepository.save(runCase);

        // AUTO-CLOSE linked bugs (READY_FOR_RETEST) for this run-case
        try {
            bugService.handleCasePassed(runCase.getId(), currentUserId);
        } catch (Exception ex) {
            // swallow or log; do NOT fail test execution because of bug auto-close issues
            log.error("Failed to auto-close bugs for runCase " + runCase.getId(), ex);
        }

        // update run
        updateRunStatus(runCase.getRun());

        // return updated data
        List<TestRunCaseStepResponse> updatedDtos = steps.stream().map(this::toDto).toList();
        return new TestCaseExecutionResponse(runCase.getId(), runCase.getStatus().name(), updatedDtos);
    }

    // --- Fail case according to Option B: only the provided step fails; others remain NOT_EXECUTED ---
    @Override
    @Transactional
    public TestCaseExecutionResponse failCase(TestCaseExecutionRequest req, Long currentUserId) {
        if (req.stepId() == null) {
            throw new IllegalArgumentException("For failCase (Option B) you must provide stepId of the failing step.");
        }

        TestRunCase runCase = findRunCase(req.runCaseId());

        // ensure steps exist and load them (initializes if needed)
        testStepExecutionService.getStepsForRunCase(runCase.getId());

        // find the specific TestRunCaseStep
        TestRunCaseStep failingStep = testRunCaseStepRepository.findByRunCaseIdAndStepId(runCase.getId(), req.stepId())
                .orElseThrow(() -> new EntityNotFoundException("Step not found for this runCase: " + req.stepId()));

        LocalDateTime now = LocalDateTime.now();
        // mark the failing step
        failingStep.setStatus(TestStepResultStatus.FAILED);
        failingStep.setActualResult(req.actualResult());
        failingStep.setExecutedBy(currentUserId);
        failingStep.setExecutedAt(now);
        failingStep.setUpdatedAt(now);
        testRunCaseStepRepository.save(failingStep);

        // Per Option B: do NOT change other steps (they remain NOT_EXECUTED)
        // update runCase status
        runCase.setStatus(TestRunCaseStatus.FAILED);
        runCase.setLastExecutedAt(now);
        testRunCaseRepository.save(runCase);

        // update run status
        updateRunStatus(runCase.getRun());

        // return updated data (only steps for this case)
        List<TestRunCaseStep> steps = testRunCaseStepRepository.findByRunCaseIdOrderByStepNumberAsc(runCase.getId());
        List<TestRunCaseStepResponse> dtos = steps.stream().map(this::toDto).toList();
        return new TestCaseExecutionResponse(runCase.getId(), runCase.getStatus().name(), dtos);
    }

    // --- Block case: mark all steps BLOCKED and set case BLOCKED ---
    @Override
    @Transactional
    public TestCaseExecutionResponse blockCase(TestCaseExecutionRequest req, Long currentUserId) {
        TestRunCase runCase = findRunCase(req.runCaseId());

        // ensure steps exist
        testStepExecutionService.getStepsForRunCase(runCase.getId());

        List<TestRunCaseStep> steps = testRunCaseStepRepository.findByRunCaseIdOrderByStepNumberAsc(runCase.getId());
        LocalDateTime now = LocalDateTime.now();
        for (TestRunCaseStep s : steps) {
            s.setStatus(TestStepResultStatus.BLOCKED);
            s.setActualResult(req.actualResult());
            s.setExecutedBy(currentUserId);
            s.setExecutedAt(now);
            s.setUpdatedAt(now);
        }
        testRunCaseStepRepository.saveAll(steps);

        runCase.setStatus(TestRunCaseStatus.BLOCKED);
        runCase.setLastExecutedAt(now);
        testRunCaseRepository.save(runCase);

        updateRunStatus(runCase.getRun());

        List<TestRunCaseStepResponse> dtos = steps.stream().map(this::toDto).toList();
        return new TestCaseExecutionResponse(runCase.getId(), runCase.getStatus().name(), dtos);
    }

    // --- Skip case: mark all steps SKIPPED and set case NOT_STARTED or SKIPPED as per your enums ---
    @Override
    @Transactional
    public TestCaseExecutionResponse skipCase(TestCaseExecutionRequest req, Long currentUserId) {
        TestRunCase runCase = findRunCase(req.runCaseId());

        // ensure steps exist
        testStepExecutionService.getStepsForRunCase(runCase.getId());

        List<TestRunCaseStep> steps = testRunCaseStepRepository.findByRunCaseIdOrderByStepNumberAsc(runCase.getId());
        LocalDateTime now = LocalDateTime.now();
        for (TestRunCaseStep s : steps) {
            s.setStatus(TestStepResultStatus.SKIPPED);
            s.setActualResult(req.actualResult());
            s.setExecutedBy(currentUserId);
            s.setExecutedAt(now);
            s.setUpdatedAt(now);
        }
        testRunCaseStepRepository.saveAll(steps);

        // you can choose SKIPPED as case status or keep NOT_STARTED; here we mark SKIPPED if enum has it
        runCase.setStatus(TestRunCaseStatus.NOT_STARTED); // or TestRunCaseStatus.SKIPPED if present
        runCase.setLastExecutedAt(now);
        testRunCaseRepository.save(runCase);

        updateRunStatus(runCase.getRun());

        List<TestRunCaseStepResponse> dtos = steps.stream().map(this::toDto).toList();
        return new TestCaseExecutionResponse(runCase.getId(), runCase.getStatus().name(), dtos);
    }

    // --- helpers ---

    private TestRunCase findRunCase(Long runCaseId) {
        return testRunCaseRepository.findById(runCaseId)
                .orElseThrow(() -> new EntityNotFoundException("TestRunCase not found: " + runCaseId));
    }

    private TestRunCaseStepResponse toDto(TestRunCaseStep s) {
        var designStep = s.getStep();
        return new TestRunCaseStepResponse(
                s.getId(),
                designStep.getId(),
                s.getStepNumber(),
                designStep.getAction(),
                designStep.getExpectedResult(),
                s.getStatus().name(),
                s.getActualResult()
        );
    }

    // Update TestRun status based on run-case collection (similar to earlier logic)
    private void updateRunStatus(TestRun run) {
        List<TestRunCase> runCases = testRunCaseRepository.findByRunId(run.getId());

        boolean anyInProgress = runCases.stream().anyMatch(rc -> rc.getStatus() == TestRunCaseStatus.IN_PROGRESS);
        boolean anyNotStarted = runCases.stream().anyMatch(rc -> rc.getStatus() == TestRunCaseStatus.NOT_STARTED);
        boolean anyFailedOrBlocked = runCases.stream().anyMatch(rc ->
                rc.getStatus() == TestRunCaseStatus.FAILED || rc.getStatus() == TestRunCaseStatus.BLOCKED);
        boolean allCompleted = !runCases.isEmpty() && runCases.stream().allMatch(rc ->
                rc.getStatus() == TestRunCaseStatus.PASSED ||
                        rc.getStatus() == TestRunCaseStatus.FAILED ||
                        rc.getStatus() == TestRunCaseStatus.BLOCKED);

        TestRunStatus newStatus;
        if (allCompleted) {
            newStatus = TestRunStatus.COMPLETED;
        } else if (anyInProgress || (!anyNotStarted && !runCases.isEmpty())) {
            newStatus = TestRunStatus.IN_PROGRESS;
        } else {
            newStatus = TestRunStatus.CREATED;
        }

        if (run.getStatus() != newStatus) {
            run.setStatus(newStatus);
            testRunRepository.save(run);
        }
    }
}
