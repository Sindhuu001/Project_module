package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.testing.TestCycleCreateRequest;
import com.example.projectmanagement.dto.testing.TestCycleSummaryResponse;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Sprint;
import com.example.projectmanagement.entity.testing.TestCycle;
import com.example.projectmanagement.enums.TestCycleStatus;
import com.example.projectmanagement.enums.TestCycleType;
import com.example.projectmanagement.repository.BugRepository;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.SprintRepository;
import com.example.projectmanagement.repository.TestCycleRepository;
import com.example.projectmanagement.repository.TestRunCaseRepository;
import com.example.projectmanagement.repository.TestRunCaseStepRepository;
import com.example.projectmanagement.repository.TestRunRepository;
import com.example.projectmanagement.service.TestCycleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestCycleServiceImpl implements TestCycleService {

        private final TestCycleRepository testCycleRepository;
        private final ProjectRepository projectRepository;
        private final SprintRepository sprintRepository;
        private final TestRunRepository testRunRepository;
        private final TestRunCaseRepository testRunCaseRepository;
        private final TestRunCaseStepRepository testRunCaseStepRepository;
        private final BugRepository bugRepository;

        // ── Create ───────────────────────────────────────────────────────────────
        @Override
        @Transactional
        public TestCycleSummaryResponse createCycle(TestCycleCreateRequest request, Long currentUserId) {

                Project project = projectRepository.findById(request.projectId())
                                .orElseThrow(() -> new EntityNotFoundException(
                                                "Project not found: " + request.projectId()));

                Sprint sprint = null;
                if (request.sprintId() != null) {
                        sprint = sprintRepository.findById(request.sprintId())
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                        "Sprint not found: " + request.sprintId()));
                }

                TestCycleType type = request.cycleType() != null
                                ? request.cycleType()
                                : TestCycleType.REGRESSION;

                LocalDateTime startDate = request.startDate() != null
                                ? request.startDate()
                                : LocalDateTime.now();

                TestCycle cycle = TestCycle.builder()
                                .project(project)
                                .sprint(sprint)
                                .name(request.name())
                                .cycleType(type)
                                .startDate(startDate)
                                .endDate(request.endDate())
                                .createdBy(currentUserId)
                                .status(TestCycleStatus.PLANNED)
                                .build();

                TestCycle saved = testCycleRepository.save(cycle);

                return new TestCycleSummaryResponse(
                                saved.getId(),
                                saved.getName(),
                                saved.getCycleType(),
                                saved.getStatus(),
                                saved.getProject().getId(),
                                saved.getSprint() != null ? saved.getSprint().getId() : null,
                                saved.getStartDate(),
                                saved.getEndDate(),
                                0,
                                0);
        }

        // ── Get All ──────────────────────────────────────────────────────────────
        @Override
        public List<TestCycleSummaryResponse> getAllCycles() {
                return testCycleRepository.findAll()
                                .stream()
                                .map(this::mapToSummaryResponse)
                                .toList();
        }

        // ── Get By Project ───────────────────────────────────────────────────────
        @Override
        public List<TestCycleSummaryResponse> getCyclesForProject(Long projectId) {
                return testCycleRepository.findByProjectId(projectId)
                                .stream()
                                .map(this::mapToSummaryResponse)
                                .toList();
        }

        // ── Get Detail ───────────────────────────────────────────────────────────
        @Override
        public TestCycleSummaryResponse getCycleDetail(Long cycleId) {
                TestCycle cycle = testCycleRepository.findById(cycleId)
                                .orElseThrow(() -> new EntityNotFoundException("Test Cycle not found: " + cycleId));
                return mapToSummaryResponse(cycle);
        }

        // ── Update ───────────────────────────────────────────────────────────────
        @Override
        @Transactional
        public TestCycleSummaryResponse updateCycle(Long cycleId,
                        TestCycleCreateRequest request,
                        Long currentUserId) {

                TestCycle cycle = testCycleRepository.findById(cycleId)
                                .orElseThrow(() -> new EntityNotFoundException("Test Cycle not found: " + cycleId));

                // Update project if changed
                if (request.projectId() != null && !request.projectId().equals(cycle.getProject().getId())) {
                        Project project = projectRepository.findById(request.projectId())
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                        "Project not found: " + request.projectId()));
                        cycle.setProject(project);
                }

                // Update sprint (allow clearing by passing null)
                if (request.sprintId() != null) {
                        Sprint sprint = sprintRepository.findById(request.sprintId())
                                        .orElseThrow(() -> new EntityNotFoundException(
                                                        "Sprint not found: " + request.sprintId()));
                        cycle.setSprint(sprint);
                } else {
                        cycle.setSprint(null);
                }

                // Update remaining fields
                cycle.setName(request.name());
                cycle.setCycleType(request.cycleType() != null ? request.cycleType() : cycle.getCycleType());
                cycle.setStatus(request.status() != null ? request.status() : cycle.getStatus());
                cycle.setStartDate(request.startDate() != null ? request.startDate() : cycle.getStartDate());
                cycle.setEndDate(request.endDate());

                TestCycle updated = testCycleRepository.save(cycle);
                return mapToSummaryResponse(updated);
        }

        // ── Delete ───────────────────────────────────────────────────────────────
        @Override
        @Transactional
        public void deleteCycle(Long cycleId) {

                // Verify the cycle exists first
                if (!testCycleRepository.existsById(cycleId)) {
                        throw new EntityNotFoundException("Test Cycle not found: " + cycleId);
                }

                // 0️⃣ Delete bugs (must be done first as they reference test_run_case_steps)
                bugRepository.deleteByRunCycleId(cycleId);
                bugRepository.flush();

                // 1️⃣ Delete run case steps
                testRunCaseStepRepository.deleteByRunCycleId(cycleId);
                testRunCaseStepRepository.flush();

                // 2️⃣ Delete run cases
                testRunCaseRepository.deleteByRunId(cycleId);
                testRunCaseRepository.flush();

                // 3️⃣ Delete runs
                testRunRepository.deleteByCycleId(cycleId);
                testRunRepository.flush();

                // 4️⃣ Delete cycle
                testCycleRepository.deleteById(cycleId);
        }

        // ── Private Mapper ───────────────────────────────────────────────────────
        private TestCycleSummaryResponse mapToSummaryResponse(TestCycle cycle) {
                int runCount = 0;
                int completedRunCount = 0;
                try {
                        runCount = testRunRepository.countByCycleId(cycle.getId());
                        completedRunCount = testRunRepository.countByCycleIdAndStatus(cycle.getId(), "COMPLETED");
                } catch (Exception ex) {
                        // safe fallback if TestRunRepository not fully wired yet
                }

                return new TestCycleSummaryResponse(
                                cycle.getId(),
                                cycle.getName(),
                                cycle.getCycleType(),
                                cycle.getStatus(),
                                cycle.getProject().getId(),
                                cycle.getSprint() != null ? cycle.getSprint().getId() : null,
                                cycle.getStartDate(),
                                cycle.getEndDate(),
                                runCount,
                                completedRunCount);
        }
}