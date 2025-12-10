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

import java.util.List;

@RestController
@RequestMapping("/api/test-execution/test-cycles")
@RequiredArgsConstructor
public class TestCycleController {

    private final TestCycleService testCycleService;

    @PostMapping
    public ResponseEntity<TestCycleSummaryResponse> createCycle(
            @Valid @RequestBody TestCycleCreateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        TestCycleSummaryResponse response = testCycleService.createCycle(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<List<TestCycleSummaryResponse>> getCyclesForProject(
            @PathVariable Long projectId
    ) {
        List<TestCycleSummaryResponse> cycles = testCycleService.getCyclesForProject(projectId);
        return ResponseEntity.ok(cycles);
    }

    @GetMapping("/{cycleId}")
    public ResponseEntity<TestCycleSummaryResponse> getCycleDetail(
            @PathVariable Long cycleId
    ) {
        TestCycleSummaryResponse cycle = testCycleService.getCycleDetail(cycleId);
        return ResponseEntity.ok(cycle);
    }
    @GetMapping("/getall")
    public ResponseEntity<List<TestCycleSummaryResponse>> getAllCycles() {
        List<TestCycleSummaryResponse> cycles = testCycleService.getAllCycles();
        return ResponseEntity.ok(cycles);
    }
}
