package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.TestScenarioCreateRequest;
import com.example.projectmanagement.dto.testing.TestScenarioSummaryResponse;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.TestScenarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.projectmanagement.dto.testing.TestScenarioUpdateRequest;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;


@RestController
@RequestMapping("/api/test-design/scenarios")
@RequiredArgsConstructor
public class TestScenarioController {

    private final TestScenarioService scenarioService;

    @PostMapping
        @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestScenarioSummaryResponse> createScenario(
            @Valid @RequestBody TestScenarioCreateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        return ResponseEntity.ok(
                scenarioService.createScenario(request, currentUser.getId())
        );
    }

    @GetMapping("/plans/{planId}")
        @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<List<TestScenarioSummaryResponse>> getScenariosForPlan(
            @PathVariable Long planId
    ) {
        return ResponseEntity.ok(
                scenarioService.getScenariosForPlan(planId)
        );
    }

    @GetMapping("/test-stories/{testStoryId}")
        @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<List<TestScenarioSummaryResponse>> getScenariosForTestStory(
            @PathVariable Long testStoryId
    ) {
        return ResponseEntity.ok(
                scenarioService.getScenariosForTestStory(testStoryId)
        );
    }

    @GetMapping("/stories/{storyId}")
        @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<List<TestScenarioSummaryResponse>> getScenariosForUserStory(
            @PathVariable Long storyId
    ) {
        return ResponseEntity.ok(
                scenarioService.getScenariosForUserStory(storyId)
        );
    }
    @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestScenarioSummaryResponse> updateScenario(
            @PathVariable Long id,
            @Valid @RequestBody TestScenarioUpdateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        return ResponseEntity.ok(
                scenarioService.updateScenario(id, request, currentUser.getId())
        );
    }

    @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<Void> deleteScenario(
            @PathVariable Long id,
            @CurrentUser UserDto currentUser
    ) {
        scenarioService.deleteScenario(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
