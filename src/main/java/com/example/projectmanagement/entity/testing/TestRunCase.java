package com.example.projectmanagement.entity.testing;

import com.example.projectmanagement.enums.TestRunCaseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_run_cases", uniqueConstraints = @UniqueConstraint(columnNames = {"run_id","test_case_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TestRunCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id")
    private TestRun run;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id")
    private TestCase testCase;

    @Column(name = "assignee_id", nullable = false)
    private Long assigneeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TestRunCaseStatus status=TestRunCaseStatus.NOT_STARTED;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "last_executed_at")
    private LocalDateTime lastExecutedAt;

}
