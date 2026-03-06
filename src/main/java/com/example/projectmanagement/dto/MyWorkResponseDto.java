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

    // ── Items grouped by project ──────────────────────────────────────────────
    // Key = projectId, Value = ordered list of work items
    private List<ProjectWorkGroup> projects;

    // ── Test work (separate section for QA users) ─────────────────────────────
    private List<TestWorkItemDto> testWork;

    // ── Manager accountability items (items created by user, assigned to others)
    private List<WorkItemDto> managerItems;

    // ─────────────────────────────────────────────────────────────────────────

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectWorkGroup {
        private Long   projectId;
        private String projectName;
        private String urgencyFlag;   // "OVERDUE" | "DUE_TODAY" | "DUE_THIS_WEEK" | "NONE"
        private int    overdueCount;
        private int    dueTodayCount;
        private List<WorkItemDto> items;
    }
}