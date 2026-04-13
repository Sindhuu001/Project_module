package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.TestCaseCreateRequest;
import com.example.projectmanagement.dto.testing.TestCaseDetailResponse;
import com.example.projectmanagement.dto.testing.TestCaseSummaryResponse;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.TestCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.projectmanagement.dto.testing.TestCaseUpdateRequest;

import java.util.List;

@RestController
@RequestMapping("/api/test-design/test-cases")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseService testCaseService;

    @PostMapping
    public ResponseEntity<TestCaseSummaryResponse> createTestCase(
            @Valid @RequestBody TestCaseCreateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        TestCaseSummaryResponse response = testCaseService.createTestCase(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scenarios/{scenarioId}")
    public ResponseEntity<List<TestCaseSummaryResponse>> getCasesForScenario(
            @PathVariable Long scenarioId
    ) {
        return ResponseEntity.ok(
                testCaseService.getCasesForScenario(scenarioId)
        );
    }

    @GetMapping("/{caseId}")
    public ResponseEntity<TestCaseDetailResponse> getCaseDetail(
            @PathVariable Long caseId
    ) {
        return ResponseEntity.ok(
                testCaseService.getCaseDetail(caseId)
        );
    }
    @GetMapping("/getcases/{projectId}")
    public ResponseEntity<List<TestCaseSummaryResponse>> getCasesForProject(
            @PathVariable Long projectId
    ) {
        return ResponseEntity.ok(
                testCaseService.getCasesForProject(projectId)
        );
    }
    // ---------------------------------------------------------
    // UPDATE (EDIT)
    // ---------------------------------------------------------
    @PutMapping("/{caseId}")
    public ResponseEntity<TestCaseSummaryResponse> updateTestCase(
            @PathVariable Long caseId,
            @Valid @RequestBody TestCaseUpdateRequest request, // Or reuse TestCaseCreateRequest if identical
            @CurrentUser UserDto currentUser
    ) {
        // Passing the user ID in case your service tracks who updated the record
        TestCaseSummaryResponse response = testCaseService.updateTestCase(caseId, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    @DeleteMapping("/{caseId}")
    public ResponseEntity<Void> deleteTestCase(
            @PathVariable("caseId") Long caseId 
    ) {
        testCaseService.deleteTestCase(caseId);
        return ResponseEntity.noContent().build(); 
    }

  


}       