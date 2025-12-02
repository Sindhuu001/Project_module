package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.TestRunCreateRequest;
import com.example.projectmanagement.dto.testing.TestRunSummaryResponse;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.TestRunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-execution/test-runs")
@RequiredArgsConstructor
public class TestRunController {

    private final TestRunService testRunService;

    @PostMapping
    public ResponseEntity<TestRunSummaryResponse> createRun(
            @Valid @RequestBody TestRunCreateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        return ResponseEntity.ok(
                testRunService.createRun(request, currentUser.getId())
        );
    }

    @GetMapping("/cycles/{cycleId}")
    public ResponseEntity<List<TestRunSummaryResponse>> getRunsForCycle(
            @PathVariable Long cycleId
    ) {
        return ResponseEntity.ok(
                testRunService.getRunsForCycle(cycleId)
        );
    }

    @GetMapping("/{runId}")
    public ResponseEntity<TestRunSummaryResponse> getRunDetail(
            @PathVariable Long runId
    ) {
        return ResponseEntity.ok(
                testRunService.getRunDetail(runId)
        );
    }
}
