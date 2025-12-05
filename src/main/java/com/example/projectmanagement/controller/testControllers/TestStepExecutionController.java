package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.AddAdHocStepRequest;
import com.example.projectmanagement.dto.testing.TestRunCaseStepResponse;
import com.example.projectmanagement.dto.testing.TestStepExecutionRequest;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.TestStepExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-execution")
@RequiredArgsConstructor
public class TestStepExecutionController {

    private final TestStepExecutionService service;

    @GetMapping("/run-cases/{runCaseId}/steps")
    public ResponseEntity<List<TestRunCaseStepResponse>> getStepsForRunCase(@PathVariable Long runCaseId) {
        return ResponseEntity.ok(service.getStepsForRunCase(runCaseId));
    }

    @PostMapping("/steps/execute")
    public ResponseEntity<TestRunCaseStepResponse> executeStep(
            @Valid @RequestBody TestStepExecutionRequest request,
            @CurrentUser UserDto currentUser
    ) {
        return ResponseEntity.ok(service.executeStep(request, currentUser.getId()));
    }

    @PostMapping("/run-cases/{runCaseId}/ad-hoc-steps")
    public ResponseEntity<TestRunCaseStepResponse> addAdHocStep(
            @PathVariable Long runCaseId,
            @Valid @RequestBody AddAdHocStepRequest request,
            @CurrentUser UserDto currentUser
    ) {
        return ResponseEntity.ok(service.addAdHocStep(runCaseId, request, currentUser.getId()));
    }
}
