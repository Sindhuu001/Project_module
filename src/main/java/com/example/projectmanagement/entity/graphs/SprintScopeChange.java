package com.example.projectmanagement.entity.graphs;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.projectmanagement.entity.Sprint;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "sprint_scope_changes")
@Data
@NoArgsConstructor
public class SprintScopeChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;

    @Column(name = "sprint_day_number")
    private Integer sprintDayNumber;          // which day of the sprint this happened

    @Column(name = "issue_id")
    private Long issueId;

    @Column(name = "issue_title")
    private String issueTitle;               // snapshot of title at time of change

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false)
    private IssueType issueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private ChangeType changeType;

    @Column(name = "old_story_points")
    private Integer oldStoryPoints;

    @Column(name = "new_story_points")
    private Integer newStoryPoints;

    @Column(name = "points_delta")
    private Integer pointsDelta;

    @Column(name = "changed_by")
    private Long changedBy;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    public enum IssueType {
        STORY, TASK
    }

    public enum ChangeType {
        ADDED_TO_SPRINT,
        REMOVED_FROM_SPRINT,
        STORY_POINTS_CHANGED,
        STATUS_CHANGED_TO_DONE,
        STATUS_REOPENED
    }
}