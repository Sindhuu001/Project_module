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
            String search,
            Pageable pageable
    ) {
        String searchTerm = normalizeSearch(search);

        if (issueType == null) {
            return fetchAllIssues(projectId, issueStatus, sprintId, searchTerm, pageable);
        }

        return switch (issueType) {
            case Epic ->
                    epicRepository.findEpicsWithRiskSummary(
                            projectId, LinkedType.Epic, issueStatus, searchTerm, pageable
                    );

            case Story ->
                    storyRepository.findStoriesWithRiskSummary(
                            projectId, LinkedType.Story, issueStatus, sprintId, searchTerm, pageable
                    );

            case Task ->
                    taskRepository.findTasksWithRiskSummary(
                            projectId, LinkedType.Task, issueStatus, sprintId, searchTerm, pageable
                    );

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
            String search,
            Pageable pageable
    ) {
        Page<RiskIssueSummaryDTO> epics =
                epicRepository.findEpicsWithRiskSummary(
                        projectId, LinkedType.Epic, issueStatus, search, pageable
                );

        Page<RiskIssueSummaryDTO> stories =
                storyRepository.findStoriesWithRiskSummary(
                        projectId, LinkedType.Story, issueStatus, sprintId, search, pageable
                );

        Page<RiskIssueSummaryDTO> tasks =
                taskRepository.findTasksWithRiskSummary(
                        projectId, LinkedType.Task, issueStatus, sprintId, search, pageable
                );

        List<RiskIssueSummaryDTO> combined =
                Stream.of(epics, stories, tasks)
                        .flatMap(p -> p.getContent().stream())
                        .toList();

        return new PageImpl<>(combined, pageable, combined.size());
    }

    private String normalizeSearch(String search) {
        if (search == null || search.trim().isEmpty()) {
            return null;
        }
        return search.trim().toLowerCase();
    }
}