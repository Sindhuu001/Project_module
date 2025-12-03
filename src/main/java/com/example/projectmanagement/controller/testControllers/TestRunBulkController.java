package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.testing.BulkAssignRequest;
import com.example.projectmanagement.dto.testing.BulkExecutionRequest;
import com.example.projectmanagement.dto.testing.CloneRunRequest;
import com.example.projectmanagement.dto.testing.TestRunSummaryResponse;
import com.example.projectmanagement.service.TestRunBulkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/test-execution/test-runs")
@RequiredArgsConstructor
public class TestRunBulkController {

    private final TestRunBulkService bulkService;

    // Bulk assign run-cases to a tester (overwrites)
    @PostMapping("/{runId}/bulk-assign")
    public ResponseEntity<Void> bulkAssign(
            @PathVariable Long runId,
            @Valid @RequestBody BulkAssignRequest req,
            Principal principal
    ) {
        Long userId = principal == null ? null : Long.parseLong(principal.getName());
        bulkService.bulkAssign(runId, req, userId);
        return ResponseEntity.ok().build();
    }

    // Bulk mark PASS
    @PostMapping("/{runId}/bulk-pass")
    public ResponseEntity<Void> bulkPass(
            @PathVariable Long runId,
            @Valid @RequestBody BulkExecutionRequest req,
            Principal principal
    ) {
        Long userId = principal == null ? null : Long.parseLong(principal.getName());
        bulkService.bulkPass(runId, req, userId);
        return ResponseEntity.ok().build();
    }

    // Bulk mark SKIP
    @PostMapping("/{runId}/bulk-skip")
    public ResponseEntity<Void> bulkSkip(
            @PathVariable Long runId,
            @Valid @RequestBody BulkExecutionRequest req,
            Principal principal
    ) {
        Long userId = principal == null ? null : Long.parseLong(principal.getName());
        bulkService.bulkSkip(runId, req, userId);
        return ResponseEntity.ok().build();
    }

    // Clone next run within cycle (returns new run summary)
    @PostMapping("/cycles/{cycleId}/clone-next-run")
    public ResponseEntity<TestRunSummaryResponse> cloneNextRun(
            @PathVariable Long cycleId,
            @Valid @RequestBody CloneRunRequest req,
            Principal principal
    ) {
        Long userId = principal == null ? null : Long.parseLong(principal.getName());
        TestRunSummaryResponse resp = bulkService.cloneNextRun(cycleId, req, userId);
        return ResponseEntity.ok(resp);
    }
}

