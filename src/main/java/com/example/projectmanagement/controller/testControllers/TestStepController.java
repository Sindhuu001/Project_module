package com.example.projectmanagement.controller.testControllers;


import com.example.projectmanagement.dto.testing.TestStepCreateRequest;
import com.example.projectmanagement.dto.testing.TestStepResponse;
import com.example.projectmanagement.dto.testing.TestStepsReorderRequest;
import com.example.projectmanagement.service.TestStepService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-design/steps")
@RequiredArgsConstructor
public class TestStepController {

    private final TestStepService testStepService;

    @GetMapping("/test-cases/{caseId}")
    public ResponseEntity<List<TestStepResponse>> getStepsForCase(
            @PathVariable Long caseId
    ) {
        return ResponseEntity.ok(
                testStepService.getStepsForCase(caseId)
        );
    }

    @PostMapping("/test-cases/{caseId}")
    public ResponseEntity<List<TestStepResponse>> addStepsToCase(
            @PathVariable Long caseId,
            @Valid @RequestBody List<TestStepCreateRequest> steps
    ) {
        return ResponseEntity.ok(
                testStepService.addStepsToCase(caseId, steps)
        );
    }

    @PutMapping("/{stepId}")
    public ResponseEntity<TestStepResponse> updateStep(
            @PathVariable Long stepId,
            @Valid @RequestBody TestStepCreateRequest request
    ) {
        return ResponseEntity.ok(
                testStepService.updateStep(stepId, request)
        );
    }

    @PutMapping("/test-cases/{caseId}/reorder")
    public ResponseEntity<List<TestStepResponse>> reorderSteps(
            @PathVariable Long caseId,
            @Valid @RequestBody TestStepsReorderRequest request
    ) {
        return ResponseEntity.ok(
                testStepService.reorderSteps(caseId, request)
        );
    }
}
