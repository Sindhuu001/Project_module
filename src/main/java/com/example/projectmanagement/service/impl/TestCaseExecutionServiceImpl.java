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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TestCaseExecutionServiceImpl implements TestCaseExecutionService {

    private final TestRunCaseRepository testRunCaseRepository;
    private final TestRunCaseStepRepository testRunCaseStepRepository;
    private final TestRunRepository testRunRepository;
    private final TestStepExecutionService testStepExecutionService;
    private final BugService bugService;

    @Override
    @Transactional
    public TestCaseExecutionResponse passCase(TestCaseExecutionRequest req, Long currentUserId) {
        TestRunCase runCase = findRunCase(req.runCaseId());
        testStepExecutionService.getStepsForRunCase(runCase.getId());
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

        runCase.setStatus(TestRunCaseStatus.PASSED);
        runCase.setLastExecutedAt(now);
        testRunCaseRepository.save(runCase);

        try {
            bugService.handleCasePassed(runCase.getId(), currentUserId);
        } catch (Exception ex) {
            log.error("Failed to auto-close bugs for runCase " + runCase.getId(), ex);
        }

        updateRunStatus(runCase.getRun());

        List<TestRunCaseStepResponse> updatedDtos = testStepExecutionService.getStepsForRunCase(runCase.getId());
        return new TestCaseExecutionResponse(runCase.getId(), runCase.getStatus().name(), updatedDtos);
    }

    @Override
    @Transactional
    public TestCaseExecutionResponse failCase(TestCaseExecutionRequest req, Long currentUserId) {
        if (req.stepId() == null) {
            throw new IllegalArgumentException("For failCase, you must provide the stepId of the failing step.");
        }
        TestRunCase runCase = findRunCase(req.runCaseId());
        testStepExecutionService.getStepsForRunCase(runCase.getId());

        TestRunCaseStep failingStep = testRunCaseStepRepository.findById(req.stepId())
                .orElseThrow(() -> new EntityNotFoundException("Step not found for this runCase: " + req.stepId()));

        LocalDateTime now = LocalDateTime.now();
        failingStep.setStatus(TestStepResultStatus.FAILED);
        failingStep.setActualResult(req.actualResult());
        failingStep.setExecutedBy(currentUserId);
        failingStep.setExecutedAt(now);
        failingStep.setUpdatedAt(now);
        testRunCaseStepRepository.save(failingStep);

        runCase.setStatus(TestRunCaseStatus.FAILED);
        runCase.setLastExecutedAt(now);
        testRunCaseRepository.save(runCase);

        updateRunStatus(runCase.getRun());

        List<TestRunCaseStepResponse> dtos = testStepExecutionService.getStepsForRunCase(runCase.getId());
        return new TestCaseExecutionResponse(runCase.getId(), runCase.getStatus().name(), dtos);
    }

    @Override
    @Transactional
    public TestCaseExecutionResponse blockCase(TestCaseExecutionRequest req, Long currentUserId) {
        TestRunCase runCase = findRunCase(req.runCaseId());
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

        List<TestRunCaseStepResponse> dtos = testStepExecutionService.getStepsForRunCase(runCase.getId());
        return new TestCaseExecutionResponse(runCase.getId(), runCase.getStatus().name(), dtos);
    }

    @Override
    @Transactional
    public TestCaseExecutionResponse skipCase(TestCaseExecutionRequest req, Long currentUserId) {
        TestRunCase runCase = findRunCase(req.runCaseId());
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

        runCase.setStatus(TestRunCaseStatus.NOT_STARTED);
        runCase.setLastExecutedAt(now);
        testRunCaseRepository.save(runCase);

        updateRunStatus(runCase.getRun());

        List<TestRunCaseStepResponse> dtos = testStepExecutionService.getStepsForRunCase(runCase.getId());
        return new TestCaseExecutionResponse(runCase.getId(), runCase.getStatus().name(), dtos);
    }

    private TestRunCase findRunCase(Long runCaseId) {
        return testRunCaseRepository.findById(runCaseId)
                .orElseThrow(() -> new EntityNotFoundException("TestRunCase not found: " + runCaseId));
    }

    private void updateRunStatus(TestRun run) {
        List<TestRunCase> runCases = testRunCaseRepository.findByRunId(run.getId());
        boolean allCompleted = !runCases.isEmpty() && runCases.stream().allMatch(rc ->
                rc.getStatus() == TestRunCaseStatus.PASSED ||
                rc.getStatus() == TestRunCaseStatus.FAILED ||
                rc.getStatus() == TestRunCaseStatus.BLOCKED);

        if (allCompleted) {
            run.setStatus(TestRunStatus.COMPLETED);
            testRunRepository.save(run);
        }
    }
}
