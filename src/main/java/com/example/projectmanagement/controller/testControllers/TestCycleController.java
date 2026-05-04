package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.TestCycleCreateRequest;
import com.example.projectmanagement.dto.testing.TestCycleSummaryResponse;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.TestCycleService;
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
@RequestMapping("/api/test-execution/test-cycles")
@RequiredArgsConstructor
public class TestCycleController {

    private final TestCycleService testCycleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestCycleSummaryResponse> createCycle(
            @Valid @RequestBody TestCycleCreateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        TestCycleSummaryResponse response = testCycleService.createCycle(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<List<TestCycleSummaryResponse>> getCyclesForProject(
            @PathVariable Long projectId
    ) {
        List<TestCycleSummaryResponse> cycles = testCycleService.getCyclesForProject(projectId);
        return ResponseEntity.ok(cycles);
    }

    @GetMapping("/{cycleId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestCycleSummaryResponse> getCycleDetail(
            @PathVariable Long cycleId
    ) {
        TestCycleSummaryResponse cycle = testCycleService.getCycleDetail(cycleId);
        return ResponseEntity.ok(cycle);
    }

    @GetMapping("/getall")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<List<TestCycleSummaryResponse>> getAllCycles() {
        List<TestCycleSummaryResponse> cycles = testCycleService.getAllCycles();
        return ResponseEntity.ok(cycles);
    }

    @PutMapping("/{cycleId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestCycleSummaryResponse> updateCycle(
            @PathVariable Long cycleId,
            @Valid @RequestBody TestCycleCreateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        TestCycleSummaryResponse updated = testCycleService.updateCycle(cycleId, request, currentUser.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{cycleId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<Void> deleteCycle(
            @PathVariable Long cycleId
    ) {
        testCycleService.deleteCycle(cycleId);
        return ResponseEntity.noContent().build();
    }
}