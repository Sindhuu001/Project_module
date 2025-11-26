package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.testing.TestRunCreateRequest;
import com.example.projectmanagement.dto.testing.TestRunSummaryResponse;
import com.example.projectmanagement.entity.testing.TestCycle;
import com.example.projectmanagement.entity.testing.TestRun;
import com.example.projectmanagement.enums.TestCycleStatus;
import com.example.projectmanagement.enums.TestRunCaseStatus;
import com.example.projectmanagement.enums.TestRunStatus;
import com.example.projectmanagement.repository.TestCycleRepository;
import com.example.projectmanagement.repository.TestRunCaseRepository;
import com.example.projectmanagement.repository.TestRunRepository;
import com.example.projectmanagement.service.TestRunService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestRunServiceImpl implements TestRunService {

    private final TestRunRepository testRunRepository;
    private final TestCycleRepository testCycleRepository;
    private final TestRunCaseRepository testRunCaseRepository;

    @Override
    @Transactional
    public TestRunSummaryResponse createRun(TestRunCreateRequest request, Long currentUserId) {

        TestCycle cycle = testCycleRepository.findById(request.cycleId())
                .orElseThrow(() ->
                        new EntityNotFoundException("Test Cycle not found: " + request.cycleId())
                );

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

        TestRun saved = testRunRepository.save(run);

        // Move cycle to IN_PROGRESS when first run is created
        if (cycle.getStatus() == TestCycleStatus.PLANNED) {
            cycle.setStatus(TestCycleStatus.IN_PROGRESS);
        }

        return new TestRunSummaryResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getStatus().name(),
                saved.getCycle().getId(),
                0,
                0
        );
    }

    @Override
    public List<TestRunSummaryResponse> getRunsForCycle(Long cycleId) {

        List<TestRun> runs = testRunRepository.findByCycleId(cycleId);

        return runs.stream()
                .map(run -> {

                    int caseCount = 0;
                    int completedCount = 0;

                    try {
                        caseCount = testRunCaseRepository.countByRunId(run.getId());
                        completedCount = testRunCaseRepository.countByRunIdAndStatus(
                                run.getId(), TestRunCaseStatus.PASSED
                        );
                    } catch (Exception ignored) {}

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

        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Test Run not found: " + runId)
                );

        int caseCount = 0;
        int completedCount = 0;

        try {
            caseCount = testRunCaseRepository.countByRunId(runId);
            completedCount = testRunCaseRepository.countByRunIdAndStatus(
                    runId, TestRunCaseStatus.PASSED
            );
        } catch (Exception ignored) {}

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
}
