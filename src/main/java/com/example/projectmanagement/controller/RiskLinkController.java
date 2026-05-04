package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.IssueTypeRiskCountDTO;
import com.example.projectmanagement.dto.RiskLinkRequest;
import com.example.projectmanagement.dto.RiskLinkResponse;
import com.example.projectmanagement.service.RiskLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;


@RestController
@RequestMapping("/api/risk-links")
public class RiskLinkController {

    @Autowired
    private RiskLinkService linkService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<RiskLinkResponse> create(@RequestBody RiskLinkRequest request) {
        return ResponseEntity.ok(linkService.createLink(request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<RiskLinkResponse> update(@PathVariable Long id, @RequestBody RiskLinkRequest request) {
        return ResponseEntity.ok(linkService.updateLink(id, request));
    }

    @GetMapping("/risk/{riskId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<RiskLinkResponse>> getByRisk(@PathVariable Long riskId) {
        return ResponseEntity.ok(linkService.getLinksByRiskId(riskId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        linkService.deleteLink(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/risk-summary/by-issue-type")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<IssueTypeRiskCountDTO>> getRiskSummaryByIssueType(
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(
                linkService.getRiskCountByIssueType(projectId)
        );
    }

}
