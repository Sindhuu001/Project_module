package com.example.projectmanagement.entity.testing;

import com.example.projectmanagement.enums.TestRunStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "test_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cycle_id")
    private TestCycle cycle;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestRunStatus status;

    private String description;

    // Who created this run (QA Lead)
    @Column(name = "created_by")
    private Long createdBy;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // When run was actually executed (optional: not used much at run level)
    @Column(name = "executed_by")
    private Long executedBy;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
