package com.example.projectmanagement.dto.testing;

import com.example.projectmanagement.enums.BugPriority;
import com.example.projectmanagement.enums.BugSeverity;
import com.example.projectmanagement.enums.BugStatus;
import com.example.projectmanagement.enums.BugType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BugDetailResponse {
    private Long id;
    private String title;
    private String description;
    private String reproductionSteps;
    private String expectedResult;
    private String actualResult;
    private BugStatus status;
    private BugSeverity severity;
    private BugPriority priority;
    private BugType type;
    private Long reporter;
    private Long assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ProjectInfo project;
    private TestStoryInfo testStory;
    private TestScenarioInfo testScenario;
    private TestCaseInfo testCase;
    private TestRunCaseInfo runCase;
    private TestRunCaseStepInfo runCaseStep;

    // Nested DTOs for related entity info
    public record ProjectInfo(Long id, String name) {}
    public record TestStoryInfo(Long id, String title) {}
    public record TestScenarioInfo(Long id, String title) {}
    public record TestCaseInfo(Long id, String title) {}
    public record TestRunCaseInfo(Long id, String title) {}
    public record TestRunCaseStepInfo(Long id, String stepDescription) {}
}
