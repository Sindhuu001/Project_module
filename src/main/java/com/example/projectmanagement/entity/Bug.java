package com.example.projectmanagement.entity;

import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.entity.Sprint;
import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.entity.testing.*;
import com.example.projectmanagement.enums.BugPriority;
import com.example.projectmanagement.enums.BugSeverity;
import com.example.projectmanagement.enums.BugStatus;
import com.example.projectmanagement.enums.BugType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bugs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ==============================
       PROJECT / AGILE LINKS
       ============================== */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    /* =================================================
       TESTING EXECUTION LINKS
       ================================================= */

    // Entire test case execution that failed and produced this bug
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_case_id")
    private TestRunCase runCase;

    // Specific failing step (optional)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_case_step_id")
    private TestRunCaseStep runCaseStep;

    // These provide traceability for Test Reports (Run → Cycle → Plan)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    private TestRun testRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id")
    private TestCycle testCycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id")
    private TestCase testCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_scenario_id")
    private TestScenario testScenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_story_id")
    private TestStory testStory;

    /* ==============================
       BUG DETAILS
       ============================== */

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BugStatus status;

    /* ==============================
       DESCRIPTION FIELDS
       ============================== */

    @Column(name = "actual_result", columnDefinition = "text")
    private String actualResult;

    @Column(name = "expected_result", columnDefinition = "text")
    private String expectedResult;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "reproduction_steps", columnDefinition = "text")
    private String reproductionSteps;

    /* ==============================
       USERS
       ============================== */

    // reporter = created_by
    @Column(name = "created_by", nullable = false)
    private Long reporter;

    // assigned developer
    @Column(name = "assigned_to")
    private Long assignedTo;

    /* ==============================
       TIMESTAMPS
       ============================== */

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;
}
