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

import java.util.List;

@RestController
@RequestMapping("/api/test-design/plans")
@RequiredArgsConstructor
public class TestPlanController {

    private final TestPlanService testPlanService;

    // Create test plan
    @PostMapping
    public ResponseEntity<TestPlanSummaryResponse> createPlan(
            @Valid @RequestBody TestPlanCreateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        TestPlanSummaryResponse response = testPlanService.createPlan(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    // Get plans for a project
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<TestPlanSummaryResponse>> getPlansForProject(
            @PathVariable Long projectId
    ) {
        List<TestPlanSummaryResponse> plans = testPlanService.getPlansForProject(projectId);
        return ResponseEntity.ok(plans);
    }

    // Get plan detail (with counts)
    @GetMapping("/{planId}")
    public ResponseEntity<TestPlanSummaryResponse> getPlanDetail(
            @PathVariable Long planId
    ) {
        TestPlanSummaryResponse plan = testPlanService.getPlanDetail(planId);
        return ResponseEntity.ok(plan);
    }
}
