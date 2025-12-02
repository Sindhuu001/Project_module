package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.RiskIssueSummaryDTO;
import com.example.projectmanagement.entity.RiskLink.LinkedType;
import com.example.projectmanagement.repository.*;
import com.example.projectmanagement.service.RiskIssueQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskIssueQueryServiceImpl implements RiskIssueQueryService {

    private final RiskEpicRepository epicRepository;
    private final RiskStoryRepository storyRepository;
    private final RiskTaskRepository taskRepository;

    @Override
    public Page<RiskIssueSummaryDTO> getIssuesWithRisks(
            Long projectId,
            LinkedType issueType,  // <- use LinkedType, not String
            String issueStatus,
            Long sprintId,
            Pageable pageable
    ) {
        return switch (issueType) {
            case Epic ->
                    epicRepository.findEpicsWithRiskSummary(
                            projectId,
                            issueType,
                            issueStatus,
                            pageable
                    );
            case Story ->
                    storyRepository.findStoriesWithRiskSummary(
                            projectId,
                            issueType,
                            issueStatus,
                            sprintId,
                            pageable
                    );
            case Task ->
                    taskRepository.findTasksWithRiskSummary(
                            projectId,
                            issueType,
                            issueStatus,
                            sprintId,
                            pageable
                    );
            default -> throw new IllegalArgumentException("Unsupported issue type: " + issueType);
        };
    }

}
