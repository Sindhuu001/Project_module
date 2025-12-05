package com.example.projectmanagement.entity.testing;

import com.example.projectmanagement.enums.TestStepResultStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_run_case_steps") // Removed unique constraint to allow null step_id
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRunCaseStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_case_id")
    private TestRunCase runCase;

    // Link to the original blueprint step. Can be NULL for ad-hoc steps.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = true)
    private TestStep step;

    // --- Fields for Ad-Hoc Steps ---
    // These are populated directly for ad-hoc steps, or can be copied from the blueprint for traceability.
    @Column(name = "action", length = 2000)
    private String action;

    @Column(name = "expected_result", length = 2000)
    private String expectedResult;
    // ---------------------------------

    @Column(name = "step_number")
    private Integer stepNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestStepResultStatus status;

    @Column(name = "actual_result", length = 2000)
    private String actualResult;

    @Column(name = "executed_by")
    private Long executedBy;

    @Column(name = "created_by")
    private Long createdBy;



    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
