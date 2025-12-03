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

import java.util.List;

@RestController
@RequestMapping("/api/test-design/scenarios")
@RequiredArgsConstructor
public class TestScenarioController {

    private final TestScenarioService scenarioService;

    @PostMapping
    public ResponseEntity<TestScenarioSummaryResponse> createScenario(
            @Valid @RequestBody TestScenarioCreateRequest request,
            @CurrentUser UserDto currentUser
    ) {
        return ResponseEntity.ok(
                scenarioService.createScenario(request, currentUser.getId())
        );
    }

    @GetMapping("/plans/{planId}")
    public ResponseEntity<List<TestScenarioSummaryResponse>> getScenariosForPlan(
            @PathVariable Long planId
    ) {
        return ResponseEntity.ok(
                scenarioService.getScenariosForPlan(planId)
        );
    }

    @GetMapping("/test-stories/{testStoryId}")
    public ResponseEntity<List<TestScenarioSummaryResponse>> getScenariosForTestStory(
            @PathVariable Long testStoryId
    ) {
        return ResponseEntity.ok(
                scenarioService.getScenariosForTestStory(testStoryId)
        );
    }

    @GetMapping("/stories/{storyId}")
    public ResponseEntity<List<TestScenarioSummaryResponse>> getScenariosForUserStory(
            @PathVariable Long storyId
    ) {
        return ResponseEntity.ok(
                scenarioService.getScenariosForUserStory(storyId)
        );
    }
}
