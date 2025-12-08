package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.*;
import com.example.projectmanagement.entity.RiskLink;
import com.example.projectmanagement.entity.RiskLink.LinkedType;
import com.example.projectmanagement.service.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risks")
public class RiskController {

    @Autowired
    private RiskService riskService;

    /* ---------- CREATE ---------- */

    @PostMapping
    public RiskResponse createRisk(@RequestBody RiskRequest request) {
        return riskService.createRisk(request);
    }

    /* ---------- READ (OLD – KEEP FOR BACKWARD COMPATIBILITY) ---------- */

    @GetMapping("/project/{projectId}")
    public List<RiskResponse> getRisksByProject(@PathVariable Long projectId) {
        return riskService.getRisksByProject(projectId);
    }

    @GetMapping("/{id}")
    public RiskResponse getRisk(@PathVariable Long id) {
        return riskService.getRiskById(id);
    }

    /* ---------- ✅ NEW PAGINATED + LINKED API ---------- */

    @GetMapping("/linked")
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
    public RiskResponse updateRisk(
            @PathVariable Long id,
            @RequestBody RiskRequest request
    ) {
        return riskService.updateRisk(id, request);
    }

    @PatchMapping("/{id}/status")
    public RiskResponse updateRiskStatus(
            @PathVariable Long id,
            @RequestBody RiskStatusUpdateRequest request
    ) {
        return riskService.updateStatus(id, request);
    }

    /* ---------- DELETE ---------- */

    @DeleteMapping("/{id}")
    public void deleteRisk(@PathVariable Long id) {
        riskService.deleteRisk(id);
    }


}
