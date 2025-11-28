package com.example.projectmanagement.service.impl;


import com.example.projectmanagement.dto.testing.BugCreateRequest;
import com.example.projectmanagement.dto.testing.BugResponse;
import com.example.projectmanagement.dto.testing.BugStatusUpdateRequest;
import com.example.projectmanagement.entity.Bug;
import com.example.projectmanagement.entity.testing.TestRunCase;
import com.example.projectmanagement.entity.testing.TestRunCaseStep;
import com.example.projectmanagement.enums.BugStatus;
import com.example.projectmanagement.enums.BugSeverity;
import com.example.projectmanagement.repository.BugRepository;
import com.example.projectmanagement.repository.TestRunCaseRepository;
import com.example.projectmanagement.repository.TestRunCaseStepRepository;
import com.example.projectmanagement.repository.TestRunRepository;
import com.example.projectmanagement.service.BugService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BugServiceImpl implements BugService {

    private final BugRepository bugRepository;
    private final TestRunCaseRepository testRunCaseRepository;
    private final TestRunCaseStepRepository testRunCaseStepRepository;
    private final TestRunRepository testRunRepository;

    @Override
    @Transactional
    public BugResponse createBug(BugCreateRequest req, Long reporterId) {
        TestRunCase runCase = testRunCaseRepository.findById(req.runCaseId())
                .orElseThrow(() -> new EntityNotFoundException("TestRunCase not found: " + req.runCaseId()));

        TestRunCaseStep runCaseStep = null;
        if (req.runCaseStepId() != null) {
            runCaseStep = testRunCaseStepRepository.findById(req.runCaseStepId())
                    .orElseThrow(() -> new EntityNotFoundException("TestRunCaseStep not found: " + req.runCaseStepId()));
        }

        Bug bug = new Bug();
        bug.setTitle(req.title());
        bug.setDescription(req.description());
        bug.setReproductionSteps(req.reproductionSteps());
        bug.setExpectedResult(req.expected());
        bug.setActualResult(req.actual());

        // Enums – convert strings safely
        try {
            bug.setSeverity(BugSeverity.valueOf(req.severity()));
        } catch (Exception ex) {
            // fallback
            bug.setSeverity(BugSeverity.MINOR);
        }
        bug.setPriority(req.priority());

        bug.setReporter(reporterId);
        bug.setAssignedTo(req.assignedTo());
        bug.setStatus(BugStatus.NEW);
        bug.setCreatedAt(LocalDateTime.now());
        bug.setUpdatedAt(LocalDateTime.now());

        // Link test execution context
        bug.setRunCase(runCase);
        if (runCaseStep != null) {
            bug.setRunCaseStep(runCaseStep);
        }
        // convenience links for quick queries in UI
        bug.setTestRun(runCase.getRun());
        bug.setTestCycle(runCase.getRun().getCycle());
        bug.setTestCase(runCase.getTestCase());
        // If these navigation methods exist on your entities:
        if (runCase.getTestCase() != null && runCase.getTestCase().getScenario() != null) {
            bug.setTestScenario(runCase.getTestCase().getScenario());
            if (runCase.getTestCase().getScenario().getTestStory() != null) {
                bug.setTestStory(runCase.getTestCase().getScenario().getTestStory());
            }
        }
        if (runCase.getTestCase() != null &&
                runCase.getTestCase().getScenario() != null &&
                runCase.getTestCase().getScenario().getTestPlan() != null &&
                runCase.getTestCase().getScenario().getTestPlan().getProject() != null) {

            bug.setProject(
                    runCase.getTestCase()
                            .getScenario()
                            .getTestPlan()
                            .getProject()
            );
        }


        Bug saved = bugRepository.save(bug);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public BugResponse updateBugStatus(Long bugId, BugStatusUpdateRequest req, Long userId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new EntityNotFoundException("Bug not found: " + bugId));

        String requested = req.status();
        BugStatus newStatus;
        try {
            newStatus = BugStatus.valueOf(requested);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown bug status: " + requested);
        }

        // If developer marks FIXED, we move to READY_FOR_RETEST to indicate QA should retest.
        if (newStatus == BugStatus.FIXED) {
            bug.setStatus(BugStatus.READY_FOR_RETEST);
        } else {
            bug.setStatus(newStatus);
        }

        bug.setAssignedTo(bug.getAssignedTo()); // keep assignedTo unchanged here; front-end can update separately
        bug.setUpdatedAt(LocalDateTime.now());

        Bug saved = bugRepository.save(bug);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void handleCasePassed(Long runCaseId, Long currentUserId) {
        // find bugs linked to this runCase that are READY_FOR_RETEST
        List<Bug> bugs = bugRepository.findByRunCaseIdAndStatusIn(runCaseId, List.of(BugStatus.READY_FOR_RETEST));

        for (Bug bug : bugs) {
            bug.setStatus(BugStatus.CLOSED);
            bug.setUpdatedAt(LocalDateTime.now());
            bugRepository.save(bug);
            // optional: add an audit note linking currentUserId that it was auto-closed
        }

        // Additionally, close bugs linked to run-case steps that are READY_FOR_RETEST
        // (rare if you link to the step not to the runCase)
        // we can also search by runCaseStepId if needed.
    }

    private BugResponse toResponse(Bug b) {
        return new BugResponse(
                b.getId(),
                b.getTitle(),
                b.getStatus(),
                b.getSeverity() != null ? b.getSeverity() : null,
                b.getPriority(),
                b.getReporter(),
                b.getAssignedTo(),
                b.getTestRun() != null ? b.getTestRun().getId() : null,
                b.getRunCase() != null ? b.getRunCase().getId() : null,
                b.getRunCaseStep() != null ? b.getRunCaseStep().getId() : null,
                b.getTestCase() != null ? b.getTestCase().getId() : null,
                b.getTestScenario() != null ? b.getTestScenario().getId() : null,
                b.getTestStory() != null ? b.getTestStory().getId() : null,
                b.getProject() != null ? b.getProject().getId() : null,
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }
}
