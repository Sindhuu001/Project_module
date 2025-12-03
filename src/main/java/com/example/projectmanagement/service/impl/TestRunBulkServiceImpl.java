package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.testing.*;
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
import com.example.projectmanagement.service.TestRunBulkService;
import com.example.projectmanagement.service.TestStepExecutionService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestRunBulkServiceImpl implements TestRunBulkService {

    private final TestRunRepository testRunRepository;
    private final TestRunCaseRepository testRunCaseRepository;
    private final TestRunCaseStepRepository testRunCaseStepRepository;
    private final TestStepRepository testStepRepository;
    private final TestStepExecutionService testStepExecutionService; // to initialize steps
    private final BugService bugService;

    // ---------------- Bulk Assign ----------------
    @Override
    @Transactional
    public void bulkAssign(Long runId, BulkAssignRequest req, Long userId) {
        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException("TestRun not found: " + runId));

        List<Long> ids = req.runCaseIds() == null ? Collections.emptyList() : req.runCaseIds();
        if (ids.isEmpty()) return;

        List<TestRunCase> runCases = testRunCaseRepository.findByIdIn(ids);
        LocalDateTime now = LocalDateTime.now();

        for (TestRunCase rc : runCases) {
            rc.setAssigneeId(req.assigneeId());
            rc.setLastExecutedAt(now);
        }

        testRunCaseRepository.saveAll(runCases);

        // update run status (fast path; will not change status usually)
        updateRunStatus(run);
    }

    // ---------------- Bulk Pass ----------------
    @Override
    @Transactional
    public void bulkPass(Long runId, BulkExecutionRequest req, Long userId) {
        List<Long> ids = req.runCaseIds() == null ? Collections.emptyList() : req.runCaseIds();
        if (ids.isEmpty()) return;

        // load runCases in one query
        List<TestRunCase> runCases = testRunCaseRepository.findByIdIn(ids);

        LocalDateTime now = LocalDateTime.now();
        List<TestRunCase> toSaveRunCases = new ArrayList<>();
        List<TestRunCaseStep> toSaveSteps = new ArrayList<>();

        for (TestRunCase rc : runCases) {
            // ensure steps initialized
            testStepExecutionService.getStepsForRunCase(rc.getId());

            // fetch step entities
            List<TestRunCaseStep> steps = testRunCaseStepRepository.findByRunCaseIdOrderByStepNumberAsc(rc.getId());

            for (TestRunCaseStep s : steps) {
                s.setStatus(TestStepResultStatus.PASSED);
                s.setActualResult(null); // optional: keep previous actualResult or overwrite with null
                s.setExecutedBy(userId);
                s.setExecutedAt(now);
                s.setUpdatedAt(now);
                toSaveSteps.add(s);
            }

            rc.setStatus(TestRunCaseStatus.PASSED);
            rc.setLastExecutedAt(now);
            rc.setLastExecutedAt(now);
            toSaveRunCases.add(rc);

            // auto-close bugs READY_FOR_RETEST for this run-case
            try {
                bugService.handleCasePassed(rc.getId(), userId);
            } catch (Exception ex) {
                // do not fail bulk pass because of bug issues; log if you have logger
            }
        }

        if (!toSaveSteps.isEmpty()) testRunCaseStepRepository.saveAll(toSaveSteps);
        if (!toSaveRunCases.isEmpty()) testRunCaseRepository.saveAll(toSaveRunCases);

        // now update parent run statuses
        if (!runCases.isEmpty()) {
            Long runIdFromList = runCases.get(0).getRun().getId();
            testRunRepository.findById(runIdFromList)
                    .ifPresent(this::updateRunStatus);
        }
    }

    // ---------------- Bulk Skip ----------------
    @Override
    @Transactional
    public void bulkSkip(Long runId, BulkExecutionRequest req, Long userId) {
        List<Long> ids = req.runCaseIds() == null ? Collections.emptyList() : req.runCaseIds();
        if (ids.isEmpty()) return;

        List<TestRunCase> runCases = testRunCaseRepository.findByIdIn(ids);

        LocalDateTime now = LocalDateTime.now();
        List<TestRunCaseStep> toSaveSteps = new ArrayList<>();
        List<TestRunCase> toSaveRunCases = new ArrayList<>();

        for (TestRunCase rc : runCases) {
            testStepExecutionService.getStepsForRunCase(rc.getId());

            List<TestRunCaseStep> steps = testRunCaseStepRepository.findByRunCaseIdOrderByStepNumberAsc(rc.getId());
            for (TestRunCaseStep s : steps) {
                s.setStatus(TestStepResultStatus.SKIPPED);
                s.setActualResult(null);
                s.setExecutedBy(userId);
                s.setExecutedAt(now);
                s.setUpdatedAt(now);
                toSaveSteps.add(s);
            }

            // choose case status for skipped. We keep NOT_STARTED to indicate no execution,
            // but you may set a specific SKIPPED status if you have it.
            rc.setStatus(TestRunCaseStatus.NOT_STARTED);
            rc.setLastExecutedAt(now);
            rc.setLastExecutedAt(now);
            toSaveRunCases.add(rc);
        }

        if (!toSaveSteps.isEmpty()) testRunCaseStepRepository.saveAll(toSaveSteps);
        if (!toSaveRunCases.isEmpty()) testRunCaseRepository.saveAll(toSaveRunCases);

        if (!runCases.isEmpty()) {
            Long runIdFromList = runCases.get(0).getRun().getId();
            testRunRepository.findById(runIdFromList)
                    .ifPresent(this::updateRunStatus);
        }
    }

    // ---------------- Clone Next Run (copy failed cases) ----------------
    @Override
    @Transactional
    public TestRunSummaryResponse cloneNextRun(Long cycleId, CloneRunRequest req, Long userId) {
        // find last run in cycle
        List<TestRun> runs = testRunRepository.findByCycleIdOrderByCreatedAtAsc(cycleId);
        if (runs.isEmpty()) {
            throw new EntityNotFoundException("No runs found for cycle: " + cycleId);
        }
        TestRun lastRun = runs.get(runs.size() - 1);

        // decide which cases to copy
        List<TestRunCase> failedCases = testRunCaseRepository.findByRunIdAndStatus(lastRun.getId(), TestRunCaseStatus.FAILED);

        if (req.includeFailedOnly() == null || req.includeFailedOnly()) {
            // keep failedCases as-is
        } else {
            // include all from last run
            failedCases = testRunCaseRepository.findByRunId(lastRun.getId());
        }

        // create new TestRun
        int existingCount = testRunRepository.countByCycleId(cycleId);
        String newName = "Run #" + (existingCount + 1);

        TestRun newRun = TestRun.builder()
                .cycle(lastRun.getCycle())
                .name(newName)
                .description("Cloned from run " + lastRun.getId())
                .status(TestRunStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .createdBy(userId)
                .build();

        TestRun savedRun = testRunRepository.save(newRun);

        // create TestRunCase rows for each failed case
        List<TestRunCase> createdRunCases = new ArrayList<>();
        List<TestRunCaseStep> createdSteps = new ArrayList<>();

        for (TestRunCase oldRc : failedCases) {
            TestRunCase newRc = TestRunCase.builder()
                    .run(savedRun)
                    .testCase(oldRc.getTestCase())
                    .assigneeId(Boolean.TRUE.equals(req.copyAssignee()) ? oldRc.getAssigneeId() : null)
                    .status(TestRunCaseStatus.NOT_STARTED)
                    .createdAt(LocalDateTime.now())
                    .lastExecutedAt(LocalDateTime.now())
                    .build();
            createdRunCases.add(newRc);
        }

        List<TestRunCase> savedRunCases = testRunCaseRepository.saveAll(createdRunCases);

        // initialize steps for each new run-case from design-time TestStep
        for (TestRunCase newRc : savedRunCases) {
            Long testCaseId = newRc.getTestCase().getId();
            List<TestStep> designSteps = testStepRepository.findByTestCaseIdOrderByStepNumberAsc(testCaseId);
            LocalDateTime now = LocalDateTime.now();

            List<TestRunCaseStep> runSteps = designSteps.stream().map(ds ->
                    TestRunCaseStep.builder()
                            .runCase(newRc)
                            .step(ds)
                            .stepNumber(ds.getStepNumber())
                            .status(TestStepResultStatus.NOT_EXECUTED)
                            .createdAt(now)
                            .updatedAt(now)
                            .build()
            ).collect(Collectors.toList());

            if (!runSteps.isEmpty()) {
                createdSteps.addAll(runSteps);
            }
        }

        if (!createdSteps.isEmpty()) testRunCaseStepRepository.saveAll(createdSteps);

        // prepare summary response (0 counts; UI will refresh)
        TestRunSummaryResponse resp = new TestRunSummaryResponse(
                savedRun.getId(),
                savedRun.getName(),
                savedRun.getDescription(),
                savedRun.getStatus().name(),
                savedRun.getCycle().getId(),
                0,
                0
        );

        return resp;
    }

    // ---------------- helper: update run status based on its cases ----------------
    @Transactional
    protected void updateRunStatus(TestRun run) {
        var runCases = testRunCaseRepository.findByRunId(run.getId());

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
