package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.testing.TestRunCaseStepResponse;
import com.example.projectmanagement.dto.testing.TestStepExecutionRequest;
import com.example.projectmanagement.service.TestStepExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/test-execution/steps")
@RequiredArgsConstructor
public class TestStepExecutionController {

    private final TestStepExecutionService testStepExecutionService;

    // Get steps + current statuses for a run-case
    @GetMapping("/run-cases/{runCaseId}")
    public ResponseEntity<List<TestRunCaseStepResponse>> getStepsForRunCase(
            @PathVariable Long runCaseId
    ) {
        return ResponseEntity.ok(
                testStepExecutionService.getStepsForRunCase(runCaseId)
        );
    }

    // Execute / update a single step
    @PostMapping("/execute")
    public ResponseEntity<TestRunCaseStepResponse> executeStep(
            @Valid @RequestBody TestStepExecutionRequest request,
            Principal principal
    ) {
        Long currentUserId = principal != null ? Long.parseLong(principal.getName()) : null;
        return ResponseEntity.ok(
                testStepExecutionService.executeStep(request, currentUserId)
        );
    }
}

