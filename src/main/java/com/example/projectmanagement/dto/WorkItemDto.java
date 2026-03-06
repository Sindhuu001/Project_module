package com.example.projectmanagement.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Unified work item DTO used exclusively by the My Work endpoint.
 * Normalises Tasks, Stories, and Bugs into a single shape for the frontend.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkItemDto {

    // ── Identity ─────────────────────────────────────────────────────────────
    private Long id;
    private String type;          // "TASK" | "STORY" | "BUG"

    // ── Core fields ──────────────────────────────────────────────────────────
    private String title;
    private String priority;      // CRITICAL | HIGH | MEDIUM | LOW
    private String statusName;    // human-readable status label
    private Long   statusId;      // null for bugs (enum-based)
    private String bugStatus;     // only set for BUG type (BugStatus enum name)

    // ── Project & Sprint context ──────────────────────────────────────────────
    private Long   projectId;
    private String projectName;
    private Long   sprintId;
    private String sprintName;

    // ── Dates ────────────────────────────────────────────────────────────────
    private LocalDateTime dueDate;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    // ── Urgency (computed server-side) ────────────────────────────────────────
    private String urgency;       // "OVERDUE" | "DUE_TODAY" | "DUE_THIS_WEEK" | "IN_SPRINT" | "FUTURE"
    private long   daysOverdue;   // > 0 means overdue, 0 otherwise
}