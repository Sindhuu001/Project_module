package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.RiskIssueSummaryDTO;
import com.example.projectmanagement.entity.RiskLink.LinkedType;
import com.example.projectmanagement.service.RiskIssueQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/risks")
@RequiredArgsConstructor
public class RiskLeftPanelController {

    private final RiskIssueQueryService service;

    @GetMapping("/issues")
    public Page<RiskIssueSummaryDTO> getIssuesWithRisks(
            @PathVariable Long projectId,
            @RequestParam LinkedType issueType,
            @RequestParam(required = false) String issueStatus,
            @RequestParam(required = false) Long sprintId,
            Pageable pageable
    ) {
        // Determine safe sort property based on issue type
        String sortProperty;
        switch (issueType) {
            case Epic -> sortProperty = "name";       // Epic.name
            case Story -> sortProperty = "title";     // Story.title
            case Task -> sortProperty = "title";      // Task.title
            default -> sortProperty = "id";           // fallback
        }

        // Create safe Pageable with correct sort
        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
//                Sort.by(sortProperty).ascending()
        );

        return service.getIssuesWithRisks(
                projectId,
                issueType,
                issueStatus,
                sprintId,
                safePageable
        );
    }

}
