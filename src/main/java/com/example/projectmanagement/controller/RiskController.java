package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.*;
import com.example.projectmanagement.entity.RiskLink;
import com.example.projectmanagement.service.RiskService;
import com.example.projectmanagement.security.CurrentUser;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risks")
public class RiskController {

    @Autowired
    private RiskService riskService;

    /* ---------- CREATE ---------- */

    @PostMapping
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','GENERAL')")
    public RiskResponse createRisk(
            @RequestBody RiskRequest request,
            @CurrentUser UserDto currentUser
    ) {
        return riskService.createRisk(request, currentUser.getId());
    }

    /* ---------- READ (OLD – KEEP FOR BACKWARD COMPATIBILITY) ---------- */

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','GENERAL')")
    public List<RiskResponse> getRisksByProject(@PathVariable Long projectId) {
        return riskService.getRisksByProject(projectId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','GENERAL')")
    public RiskResponse getRisk(@PathVariable Long id) {
        return riskService.getRiskById(id);
    }

    /* ---------- EXISTING PAGINATED + LINKED API ---------- */

    @GetMapping("/linked")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','GENERAL')")
    public RiskResponseDTO getRisksWithPagination(
            @RequestParam Long projectId,
            @RequestParam(required = false) RiskLink.LinkedType linkedType,
            @RequestParam(required = false) Long linkedId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String severity
    ) {
        return riskService.getRisksWithPagination(
                projectId,
                linkedType,
                linkedId,
                page,
                size,
                severity
        );
    }

    /* ---------- NEW LINKED RISK SEARCH API ---------- */

    @GetMapping("/linked/search")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','GENERAL')")
    public RiskResponseDTO searchLinkedRisks(
            @RequestParam Long projectId,
            @RequestParam RiskLink.LinkedType linkedType,
            @RequestParam Long linkedId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return riskService.getLinkedRisks(
                projectId,
                linkedType,
                linkedId,
                page,
                size,
                search
        );
    }

    /* ---------- UPDATE ---------- */

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','GENERAL')")
    public RiskResponse updateRisk(
            @PathVariable Long id,
            @RequestBody RiskRequest request
    ) {
        return riskService.updateRisk(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','GENERAL')")
    public RiskResponse updateRiskStatus(
            @PathVariable Long id,
            @RequestBody RiskStatusUpdateRequest request
    ) {
        return riskService.updateStatus(id, request);
    }

    /* ---------- DELETE ---------- */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','GENERAL')")
    public void deleteRisk(@PathVariable Long id) {
        riskService.deleteRisk(id);
    }
}