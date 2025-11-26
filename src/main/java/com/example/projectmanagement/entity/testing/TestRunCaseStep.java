package com.example.projectmanagement.entity.testing;

import com.example.projectmanagement.enums.TestStepResultStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_run_case_steps",
        uniqueConstraints = @UniqueConstraint(columnNames = {"run_case_id", "step_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRunCaseStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Execution context: which run-case this step result belongs to
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_case_id")
    private TestRunCase runCase;

    // Design-time step definition
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "step_id")
    private TestStep step;

    // Cached step number to avoid join for ordering
    @Column(name = "step_number")
    private Integer stepNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestStepResultStatus status;

    @Column(name = "actual_result", length = 2000)
    private String actualResult;

    @Column(name = "executed_by")
    private Long executedBy;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
