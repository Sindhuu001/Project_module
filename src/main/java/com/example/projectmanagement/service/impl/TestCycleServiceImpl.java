package com.example.projectmanagement.service.impl;


import com.example.projectmanagement.dto.testing.TestCycleCreateRequest;
import com.example.projectmanagement.dto.testing.TestCycleSummaryResponse;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Sprint;
import com.example.projectmanagement.entity.testing.TestCycle;
import com.example.projectmanagement.enums.TestCycleStatus;
import com.example.projectmanagement.enums.TestCycleType;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.SprintRepository;
import com.example.projectmanagement.repository.TestCycleRepository;
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
    private final TestRunRepository testRunRepository; // you can stub this for now if not ready

    @Override
    @Transactional
    public TestCycleSummaryResponse createCycle(TestCycleCreateRequest request, Long currentUserId) {

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new EntityNotFoundException("Project not found: " + request.projectId()));

        Sprint sprint = null;
        if (request.sprintId() != null) {
            sprint = sprintRepository.findById(request.sprintId())
                    .orElseThrow(() -> new EntityNotFoundException("Sprint not found: " + request.sprintId()));

            // optional: validate sprint.getProject().getId().equals(project.getId())
        }

        TestCycleType type = request.cycleType() != null
                ? request.cycleType()
                : TestCycleType.REGRESSION; // default type if none provided

        LocalDateTime startDate = request.startDate() != null
                ? request.startDate()
                : LocalDateTime.now();

        TestCycle cycle = TestCycle.builder()
                .project(project)
                .sprint(sprint)
                .name(request.name())
                .cycleType(type)
                .startDate(startDate)
                .endDate(request.endDate())   // can be null initially
                .createdBy(currentUserId)
                .status(TestCycleStatus.PLANNED)
                .build();

        TestCycle saved = testCycleRepository.save(cycle);

        // On creation, no runs exist yet
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
                0
        );
    }

    @Override
    public List<TestCycleSummaryResponse> getCyclesForProject(Long projectId) {
        // Load only cycles for that project; keep this lightweight
        List<TestCycle> cycles = testCycleRepository.findByProjectId(projectId);

        return cycles.stream()
                .map(cycle -> {
                    int runCount = 0;
                    int completedRunCount = 0;
                    try {
                        runCount = testRunRepository.countByCycleId(cycle.getId());
                        // if you later add status enum, adjust this
                        completedRunCount = testRunRepository.countByCycleIdAndStatus(cycle.getId(), "COMPLETED");
                    } catch (Exception ex) {
                        // if TestRunRepository is not wired yet, you can temporarily skip this
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
                            completedRunCount
                    );
                })
                .toList();
    }

    @Override
    public TestCycleSummaryResponse getCycleDetail(Long cycleId) {
        TestCycle cycle = testCycleRepository.findById(cycleId)
                .orElseThrow(() -> new EntityNotFoundException("Test Cycle not found: " + cycleId));

        int runCount = 0;
        int completedRunCount = 0;
        try {
            runCount = testRunRepository.countByCycleId(cycleId);
            completedRunCount = testRunRepository.countByCycleIdAndStatus(cycleId, "COMPLETED");
        } catch (Exception ex) {
            // same as above – safe fallback during initial stages
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
                completedRunCount

        );
    }
}

