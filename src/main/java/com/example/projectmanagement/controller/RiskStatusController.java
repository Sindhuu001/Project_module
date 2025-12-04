package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.RiskStatusCreateRequest;
import com.example.projectmanagement.dto.RiskStatusResponse;
import com.example.projectmanagement.entity.RiskStatus;
import com.example.projectmanagement.service.RiskStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.example.projectmanagement.dto.RiskStatusDto;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RiskStatusController {

    @Autowired
    private RiskStatusService riskStatusService;

    // CREATE
    @PostMapping("/risk-statuses")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<RiskStatusResponse> createRiskStatus(
            @RequestBody RiskStatusCreateRequest request
    ) {
        return ResponseEntity.ok(riskStatusService.createRiskStatus(request));
    }

    @GetMapping("/risk-statuses/{id}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<RiskStatus> getRiskStatus(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(riskStatusService.getRiskStatus(id));
    }


    // READ: GET by project
    @GetMapping("/projects/{projectId}/risk-statuses")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<RiskStatusResponse>> getRiskStatusesByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(riskStatusService.getRiskStatusesByProject(projectId));
    }

    // UPDATE
    @PutMapping("/risk-statuses/{id}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<RiskStatusResponse> updateRiskStatus(
            @PathVariable Long id,
            @RequestBody RiskStatusCreateRequest request
    ) {
        return ResponseEntity.ok(riskStatusService.updateRiskStatus(id, request));
    }

    // DELETE
    @DeleteMapping("/risk-statuses/{id}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<?> deleteRiskStatus(
            @PathVariable Long id,
            @RequestParam(required = false) Long newStatusId
    ) {
        riskStatusService.deleteRiskStatus(id, newStatusId);
        return ResponseEntity.ok().build();
    }

    // REORDER
    @PostMapping("/risk-statuses/reorder")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<RiskStatusResponse>> reorderRiskStatuses(
            @RequestBody Map<Long, Integer> newOrder
    ) {
        return ResponseEntity.ok(riskStatusService.reorderRiskStatuses(newOrder));
    }
}
