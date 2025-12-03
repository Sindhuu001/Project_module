package com.example.projectmanagement.dto;

import java.util.List;

public class ProjectIssueRiskSummaryDTO {

    private Long projectId;
    private List<IssueTypeRiskCountDTO> issueTypes;

    public ProjectIssueRiskSummaryDTO(
            Long projectId,
            List<IssueTypeRiskCountDTO> issueTypes) {
        this.projectId = projectId;
        this.issueTypes = issueTypes;
    }

    public Long getProjectId() {
        return projectId;
    }

    public List<IssueTypeRiskCountDTO> getIssueTypes() {
        return issueTypes;
    }
}

