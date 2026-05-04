package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.*;
import com.example.projectmanagement.entity.RiskLink;
import com.example.projectmanagement.entity.RiskLink.LinkedType;
import com.example.projectmanagement.service.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.projectmanagement.security.CurrentUser;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;


@RestController
@RequestMapping("/api/risks")
public class RiskController {

    @Autowired
    private RiskService riskService;

    /* ---------- CREATE ---------- */

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public RiskResponse createRisk(@RequestBody RiskRequest request,@CurrentUser UserDto currentUser) {
        return riskService.createRisk(request,currentUser.getId());
    }

    /* ---------- READ (OLD – KEEP FOR BACKWARD COMPATIBILITY) ---------- */

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public List<RiskResponse> getRisksByProject(@PathVariable Long projectId) {
        return riskService.getRisksByProject(projectId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public RiskResponse getRisk(@PathVariable Long id) {
        return riskService.getRiskById(id);
    }

    /* ---------- ✅ NEW PAGINATED + LINKED API ---------- */

    @GetMapping("/linked")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public RiskResponseDTO getRisksWithPagination(
            @RequestParam Long projectId,
            @RequestParam(required = false) RiskLink.LinkedType linkedType,
            @RequestParam(required = false) Long linkedId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String severity
    ) {
        return riskService.getRisksWithPagination(
                projectId, linkedType, linkedId, page, size, severity
        );
    }


    /* ---------- UPDATE ---------- */

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public RiskResponse updateRisk(
            @PathVariable Long id,
            @RequestBody RiskRequest request
    ) {
        return riskService.updateRisk(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public RiskResponse updateRiskStatus(
            @PathVariable Long id,
            @RequestBody RiskStatusUpdateRequest request
    ) {
        return riskService.updateStatus(id, request);
    }

    /* ---------- DELETE ---------- */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public void deleteRisk(@PathVariable Long id) {
        riskService.deleteRisk(id);
    }


}
