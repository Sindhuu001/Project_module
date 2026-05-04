package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.RiskIssueSummaryDTO;
import com.example.projectmanagement.entity.RiskLink.LinkedType;
import com.example.projectmanagement.service.RiskIssueQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;

@RestController
@RequestMapping("/api/projects/{projectId}/risks")
@RequiredArgsConstructor
public class RiskLeftPanelController {

    private final RiskIssueQueryService service;

    @GetMapping("/issues")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public Page<RiskIssueSummaryDTO> getIssuesWithRisks(
            @PathVariable Long projectId,
            @RequestParam(required = false) LinkedType issueType, // ✅ OPTIONAL
            @RequestParam(required = false) String issueStatus,
            @RequestParam(required = false) Long sprintId,
            Pageable pageable
    ) {

        // ✅ enforce safe pageable (avoid invalid sort)
        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        return service.getIssuesWithRisks(
                projectId,
                issueType,       // ✅ can be null → ALL
                issueStatus,
                sprintId,
                safePageable
        );
    }
}
