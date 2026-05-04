package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.AddCasesToRunRequest;
import com.example.projectmanagement.dto.testing.TestRunCreateRequest;
import com.example.projectmanagement.dto.testing.TestRunCaseResponse;
import com.example.projectmanagement.dto.testing.TestRunSummaryResponse;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.TestRunService;
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
@RequestMapping("/api/test-execution/test-runs")
@RequiredArgsConstructor
public class TestRunController {

    private final TestRunService testRunService;

    @PostMapping
        @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestRunSummaryResponse> createRun(
            @Valid @RequestBody TestRunCreateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        return ResponseEntity.ok(
                testRunService.createRun(request, currentUser.getId())
        );
    }

    @PostMapping("/{runId}/add-cases")
        @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<Void> addCasesToRun(
            @PathVariable Long runId,
            @Valid @RequestBody AddCasesToRunRequest request
    ) {
        testRunService.addTestCasesToRun(runId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cycles/{cycleId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<List<TestRunSummaryResponse>> getRunsForCycle(
            @PathVariable Long cycleId
    ) {
        return ResponseEntity.ok(
                testRunService.getRunsForCycle(cycleId)
        );
    }

    @GetMapping("/{runId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestRunSummaryResponse> getRunDetail(
            @PathVariable Long runId
    ) {
        return ResponseEntity.ok(
                testRunService.getRunDetail(runId)
        );
    }

    @GetMapping("/{runId}/cases")
        @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<List<TestRunCaseResponse>> getTestCasesForRun(@PathVariable Long runId) {
        return ResponseEntity.ok(testRunService.getTestCasesForRun(runId));
    }

    @PutMapping("/{runId}")
    public ResponseEntity<TestRunSummaryResponse> updateRun(
            @PathVariable Long runId,
            @Valid @RequestBody TestRunCreateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        return ResponseEntity.ok(testRunService.updateRun(runId, request, currentUser.getId()));
    }

    @DeleteMapping("/{runId}")
    public ResponseEntity<Void> deleteRun(
            @PathVariable Long runId
    ) {
        testRunService.deleteRun(runId);
        return ResponseEntity.noContent().build();
    }
}
