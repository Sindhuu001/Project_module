package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.testing.AddCasesToRunRequest;
import com.example.projectmanagement.dto.testing.TestRunCaseResponse;
import com.example.projectmanagement.dto.testing.TestRunCreateRequest;
import com.example.projectmanagement.dto.testing.TestRunSummaryResponse;
import com.example.projectmanagement.entity.testing.TestCase;
import com.example.projectmanagement.entity.testing.TestCycle;
import com.example.projectmanagement.entity.testing.TestRun;
import com.example.projectmanagement.entity.testing.TestRunCase;
import com.example.projectmanagement.enums.TestCycleStatus;
import com.example.projectmanagement.enums.TestRunCaseStatus;
import com.example.projectmanagement.enums.TestRunStatus;
import com.example.projectmanagement.repository.TestCaseRepository;
import com.example.projectmanagement.repository.TestCycleRepository;
import com.example.projectmanagement.repository.TestRunCaseRepository;
import com.example.projectmanagement.repository.TestRunRepository;
import com.example.projectmanagement.service.TestRunService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestRunServiceImpl implements TestRunService {

    private final TestRunRepository testRunRepository;
    private final TestCycleRepository testCycleRepository;
    private final TestRunCaseRepository testRunCaseRepository;
    private final TestCaseRepository testCaseRepository;

    @Override
    @Transactional
    public TestRunSummaryResponse createRun(TestRunCreateRequest request, Long currentUserId) {
        // ... existing implementation ...
        TestCycle cycle = testCycleRepository.findById(request.cycleId())
                .orElseThrow(() -> new EntityNotFoundException("Test Cycle not found: " + request.cycleId()));

        int existingRunCount = testRunRepository.countByCycleId(request.cycleId());
        String generatedName = "Run #" + (existingRunCount + 1);

        String finalName = (request.name() == null || request.name().isBlank())
                ? generatedName
                : request.name();

        TestRun run = TestRun.builder()
                .cycle(cycle)
                .name(finalName)
                .description(request.description())
                .status(TestRunStatus.CREATED)
                .executedAt(LocalDateTime.now())
                .createdBy(currentUserId)
                .build();

        TestRun savedRun = testRunRepository.save(run);

        int caseCount = 0;
        if (request.testCaseIds() != null && !request.testCaseIds().isEmpty()) {
            List<TestCase> testCases = testCaseRepository.findAllById(request.testCaseIds());
            if (testCases.size() != request.testCaseIds().size()) {
                throw new EntityNotFoundException("One or more test cases not found.");
            }

            List<TestRunCase> runCases = new ArrayList<>();
            for (TestCase tc : testCases) {
                TestRunCase newRc = TestRunCase.builder()
                        .run(savedRun)
                        .testCase(tc)
                        .status(TestRunCaseStatus.NOT_STARTED)
                        .createdAt(LocalDateTime.now())
                        .lastExecutedAt(LocalDateTime.now())
                        .build();
                runCases.add(newRc);
            }
            testRunCaseRepository.saveAll(runCases);
            caseCount = runCases.size();
        }

        if (cycle.getStatus() == TestCycleStatus.PLANNED) {
            cycle.setStatus(TestCycleStatus.IN_PROGRESS);
        }

        return new TestRunSummaryResponse(
                savedRun.getId(),
                savedRun.getName(),
                savedRun.getDescription(),
                savedRun.getStatus().name(),
                savedRun.getCycle().getId(),
                caseCount,
                0
        );
    }

    @Override
    @Transactional
    public void addTestCasesToRun(Long runId, AddCasesToRunRequest request) {
        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException("TestRun not found: " + runId));

        // 1. Fetch existing case IDs to prevent duplicates
        Set<Long> existingTestCaseIds = testRunCaseRepository.findByRunId(runId).stream()
                .map(trc -> trc.getTestCase().getId())
                .collect(Collectors.toSet());

        // 2. Filter out IDs that are already in the run
        List<Long> newTestCaseIds = request.testCaseIds().stream()
                .filter(id -> !existingTestCaseIds.contains(id))
                .collect(Collectors.toList());

        if (newTestCaseIds.isEmpty()) {
            return; // Nothing to add
        }

        // 3. Fetch the TestCase entities for the new IDs
        List<TestCase> testCasesToAdd = testCaseRepository.findAllById(newTestCaseIds);
        if (testCasesToAdd.size() != newTestCaseIds.size()) {
            throw new EntityNotFoundException("One or more test cases to add were not found.");
        }

        // 4. Create new TestRunCase records
        List<TestRunCase> newRunCases = new ArrayList<>();
        for (TestCase tc : testCasesToAdd) {
            TestRunCase newRc = TestRunCase.builder()
                    .run(run)
                    .testCase(tc)
                    .status(TestRunCaseStatus.NOT_STARTED)
                    .createdAt(LocalDateTime.now())
                    .lastExecutedAt(LocalDateTime.now())
                    .build();
            newRunCases.add(newRc);
        }

        // 5. Save the new run cases
        testRunCaseRepository.saveAll(newRunCases);
    }

    @Override
    public List<TestRunSummaryResponse> getRunsForCycle(Long cycleId) {
        // ... existing implementation ...
        List<TestRun> runs = testRunRepository.findByCycleId(cycleId);

        return runs.stream()
                .map(run -> {
                    int caseCount = testRunCaseRepository.countByRunId(run.getId());
                    int completedCount = testRunCaseRepository.countByRunIdAndStatus(run.getId(), TestRunCaseStatus.PASSED);
                    return new TestRunSummaryResponse(
                            run.getId(),
                            run.getName(),
                            run.getDescription(),
                            run.getStatus().name(),
                            run.getCycle().getId(),
                            caseCount,
                            completedCount
                    );
                })
                .toList();
    }

    @Override
    public TestRunSummaryResponse getRunDetail(Long runId) {
        // ... existing implementation ...
        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() -> new EntityNotFoundException("Test Run not found: " + runId));

        int caseCount = testRunCaseRepository.countByRunId(runId);
        int completedCount = testRunCaseRepository.countByRunIdAndStatus(runId, TestRunCaseStatus.PASSED);

        return new TestRunSummaryResponse(
                run.getId(),
                run.getName(),
                run.getDescription(),
                run.getStatus().name(),
                run.getCycle().getId(),
                caseCount,
                completedCount
        );
    }

    @Override
    public List<TestRunCaseResponse> getTestCasesForRun(Long runId) {
        List<TestRunCase> runCases = testRunCaseRepository.findByRunId(runId);
        return runCases.stream()
                .map(rc -> new TestRunCaseResponse(
                        rc.getTestCase().getId(),
                        rc.getTestCase().getTitle(),
                        rc.getTestCase().getType().name(),
                        rc.getTestCase().getPriority().name(),
                        rc.getTestCase().getStatus(),
                        rc.getAssigneeId(),
                        rc.getStatus().name()
                ))
                .collect(Collectors.toList());
    }
}
