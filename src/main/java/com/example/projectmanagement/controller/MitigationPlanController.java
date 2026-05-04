package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.MitigationPlanRequest;
import com.example.projectmanagement.dto.MitigationPlanResponse;
import com.example.projectmanagement.dto.MitigationPlanStatusPatchRequest;
import com.example.projectmanagement.service.MitigationPlanService;
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
@RequestMapping("/api/mitigation-plans")
public class MitigationPlanController {

    @Autowired
    private MitigationPlanService mitigationPlanService;

    // ------------------- CREATE -------------------
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<MitigationPlanResponse> createPlan(@RequestBody MitigationPlanRequest request) {
        MitigationPlanResponse response = mitigationPlanService.createPlan(request);
        return ResponseEntity.ok(response);
    }

    // ------------------- READ -------------------
    @GetMapping("/risk/{riskId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<MitigationPlanResponse>> getPlansByRisk(@PathVariable Long riskId) {
        List<MitigationPlanResponse> response = mitigationPlanService.getPlansByRiskId(riskId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<MitigationPlanResponse> getPlanById(@PathVariable Long id) {
        // You can implement a separate method if needed
        MitigationPlanResponse response = mitigationPlanService.updatePlan(id, null);
        return ResponseEntity.ok(response);
    }

    // ------------------- UPDATE -------------------
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<MitigationPlanResponse> updatePlan(
            @PathVariable Long id,
            @RequestBody MitigationPlanRequest request) {
        MitigationPlanResponse response = mitigationPlanService.updatePlan(id, request);
        return ResponseEntity.ok(response);
    }

    // ------------------- DELETE -------------------
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<Void> deletePlan(@PathVariable Long id) {
        mitigationPlanService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<MitigationPlanResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody MitigationPlanStatusPatchRequest request) {

        return ResponseEntity.ok(mitigationPlanService.updateStatus(id, request));
    }

}
