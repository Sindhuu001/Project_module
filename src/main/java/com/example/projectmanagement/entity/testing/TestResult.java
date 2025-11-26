package com.example.projectmanagement.entity.testing;

import com.example.projectmanagement.entity.Bug;
import com.example.projectmanagement.enums.TestResultStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_results")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_run_case_id")
    private TestRunCase testRunCase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestResultStatus status;

    @Column(name = "actual_result", columnDefinition = "text")
    private String actualResult;

    @Column(name = "executed_by", nullable = false)
    private Long executedBy;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bug_id")
    private Bug bug; // may be null if no bug raised
}

