package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.RiskIssueSummaryDTO;
import com.example.projectmanagement.entity.RiskLink.LinkedType;
import com.example.projectmanagement.repository.*;
import com.example.projectmanagement.service.RiskIssueQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RiskIssueQueryServiceImpl implements RiskIssueQueryService {

    private final RiskEpicRepository epicRepository;
    private final RiskStoryRepository storyRepository;
    private final RiskTaskRepository taskRepository;

    @Override
    public Page<RiskIssueSummaryDTO> getIssuesWithRisks(
            Long projectId,
            LinkedType issueType,
            String issueStatus,
            Long sprintId,
            Pageable pageable
    ) {

        // ✅ handle ALL first
        if (issueType == null) {
            return fetchAllIssues(projectId, issueStatus, sprintId, pageable);
        }

        // ✅ switch now SAFE (non-null)
        return switch (issueType) {
            case Epic ->
                    epicRepository.findEpicsWithRiskSummary(
                            projectId, LinkedType.Epic, issueStatus, pageable
                    );

            case Story ->
                    storyRepository.findStoriesWithRiskSummary(
                            projectId, LinkedType.Story, issueStatus, sprintId, pageable
                    );

            case Task ->
                    taskRepository.findTasksWithRiskSummary(
                            projectId, LinkedType.Task, issueStatus, sprintId, pageable
                    );

            // ✅ Explicitly reject unsupported types
            case Sprint, Bug, Release ->
                    throw new IllegalArgumentException(
                            "Issue type not supported for risk panel: " + issueType
                    );
        };
    }

    private Page<RiskIssueSummaryDTO> fetchAllIssues(
            Long projectId,
            String issueStatus,
            Long sprintId,
            Pageable pageable
    ) {

        Page<RiskIssueSummaryDTO> epics =
                epicRepository.findEpicsWithRiskSummary(
                        projectId, LinkedType.Epic, issueStatus, pageable
                );

        Page<RiskIssueSummaryDTO> stories =
                storyRepository.findStoriesWithRiskSummary(
                        projectId, LinkedType.Story, issueStatus, sprintId, pageable
                );

        Page<RiskIssueSummaryDTO> tasks =
                taskRepository.findTasksWithRiskSummary(
                        projectId, LinkedType.Task, issueStatus, sprintId, pageable
                );

        List<RiskIssueSummaryDTO> combined =
                Stream.of(epics, stories, tasks)
                        .flatMap(p -> p.getContent().stream())
                        .toList();

        return new PageImpl<>(combined, pageable, combined.size());
    }
}
