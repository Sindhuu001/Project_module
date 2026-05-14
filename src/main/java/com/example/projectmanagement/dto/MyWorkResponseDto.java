package com.example.projectmanagement.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Top-level response for GET /api/my-work
 * One payload powers both the full My Work page and any future widget.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyWorkResponseDto {

    // ── Snapshot counts (for the stat chips) ─────────────────────────────────
    private long overdueCount;
    private long dueTodayCount;
    private long dueThisWeekCount;
    private long allActiveCount;
    private long blockedCount;

    // Dashboard summary counts
    private long activeProjectCount;

    /**
     * Tasks assigned to the user whose status.sortOrder < MAX(sortOrder)
     * for their project. The highest-order column is always treated as terminal
     * (done), regardless of its name. Fully dynamic — unaffected by renames
     * or reordering of columns.
     */
    private long pendingTasksCount;

    // ── Items grouped by project ──────────────────────────────────────────────
    private List<ProjectWorkGroup> projects;

    // ── Test work (separate section for QA users) ─────────────────────────────
    private List<TestWorkItemDto> testWork;

    // ── PROJECT_MANAGER accountability items (items created by user, assigned to others)
    private List<WorkItemDto> PROJECT_MANAGERItems;

    // ─────────────────────────────────────────────────────────────────────────

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectWorkGroup {
        private Long projectId;
        private String projectName;
        private String urgencyFlag; // "OVERDUE" | "DUE_TODAY" | "DUE_THIS_WEEK" | "NONE"
        private int overdueCount;
        private int dueTodayCount;
        private List<WorkItemDto> items;
    }
}