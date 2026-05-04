package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.TestPlanCreateRequest;
import com.example.projectmanagement.dto.testing.TestPlanSummaryResponse;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.TestPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;


import java.util.List;

@RestController
@RequestMapping("/api/test-design/plans")
@RequiredArgsConstructor
public class TestPlanController {

    private final TestPlanService testPlanService;

    // Create test plan
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestPlanSummaryResponse> createPlan(
            @Valid @RequestBody TestPlanCreateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        TestPlanSummaryResponse response = testPlanService.createPlan(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    // Get plans for a project
    @GetMapping("/projects/{projectId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<List<TestPlanSummaryResponse>> getPlansForProject(
            @PathVariable Long projectId
    ) {
        List<TestPlanSummaryResponse> plans = testPlanService.getPlansForProject(projectId);
        return ResponseEntity.ok(plans);
    }

    // Get plan detail (with counts)
    @GetMapping("/{planId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestPlanSummaryResponse> getPlanDetail(
            @PathVariable Long planId
    ) {
        TestPlanSummaryResponse plan = testPlanService.getPlanDetail(planId);
        return ResponseEntity.ok(plan);
    }
     @DeleteMapping("/{planId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<Void> deleteTestPlan(@PathVariable Long planId) {
        testPlanService.deleteTestPlan(planId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/update/{planId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestPlanSummaryResponse> updateTestPlan(
            @PathVariable Long planId,
            @Valid @RequestBody TestPlanCreateRequest request
    ) {
        // Assuming you have an updatePlan method in your service
        TestPlanSummaryResponse updatedPlan = testPlanService.updatePlan(planId, request);
        return ResponseEntity.ok(updatedPlan);
    }
}
