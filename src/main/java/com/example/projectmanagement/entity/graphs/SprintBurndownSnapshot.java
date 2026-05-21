package com.example.projectmanagement.entity.graphs;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.projectmanagement.entity.Sprint;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "sprint_burndown_snapshots",
    uniqueConstraints = @UniqueConstraint(columnNames = {"sprint_id", "snapshot_date"})
)
@Data
@NoArgsConstructor
public class SprintBurndownSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "sprint_day_number", nullable = false)
    private Integer sprintDayNumber;

    @Column(name = "is_weekend", nullable = false)
    private Boolean isWeekend = false;

    // ── Story Points ──────────────────────────────────────────────

    @Column(name = "initial_story_points", nullable = false)
    private Integer initialStoryPoints;       // locked on day 1, never changes

    @Column(name = "ideal_remaining_points", nullable = false)
    private Integer idealRemainingPoints;     // linear burn target for this day

    @Column(name = "current_story_points", nullable = false)
    private Integer currentStoryPoints;       // total in sprint today

    @Column(name = "completed_story_points", nullable = false)
    private Integer completedStoryPoints;

    @Column(name = "remaining_story_points", nullable = false)
    private Integer remainingStoryPoints;

    @Column(name = "velocity_points", nullable = false)
    private Integer velocityPoints = 0;       // points burned today only

    // ── Scope Changes ─────────────────────────────────────────────

    @Column(name = "added_scope_points", nullable = false)
    private Integer addedScopePoints = 0;

    @Column(name = "removed_scope_points", nullable = false)
    private Integer removedScopePoints = 0;

    // ── Issue Counts ──────────────────────────────────────────────

    @Column(name = "total_issues", nullable = false)
    private Integer totalIssues;

    @Column(name = "completed_issues", nullable = false)
    private Integer completedIssues;

    @Column(name = "remaining_issues", nullable = false)
    private Integer remainingIssues;

    @Column(name = "velocity_issues", nullable = false)
    private Integer velocityIssues = 0;       // issues closed today only

    // ── Metadata ──────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "is_holiday", nullable = false)
private Boolean isHoliday = false;

@Column(name = "is_working_weekend", nullable = false)
private Boolean isWorkingWeekend = false;
} 
