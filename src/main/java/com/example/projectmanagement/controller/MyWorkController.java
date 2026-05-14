package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.MyWorkResponseDto;
import com.example.projectmanagement.service.MyWorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;
import java.util.Map;

@RestController
@RequestMapping("/api/my-work")
@CrossOrigin
@RequiredArgsConstructor
public class MyWorkController {

    private final MyWorkService myWorkService;

    /**
     * Main payload — tasks, stories, bugs grouped by project.
     * Called once on page load; React Query handles caching + revalidation.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','GENERAL')")
    public ResponseEntity<MyWorkResponseDto> getMyWork(@RequestParam Long userId) {
        return ResponseEntity.ok(myWorkService.getMyWork(userId));
    }

    /**
     * Completed items — fetched lazily only when user expands the Completed
     * section. Separate endpoint keeps the main payload lean.
     */
    @GetMapping("/completed")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','GENERAL')")
    public ResponseEntity<MyWorkResponseDto> getMyWorkCompleted(@RequestParam Long userId) {
        return ResponseEntity.ok(myWorkService.getMyWorkCompleted(userId));
    }

    /**
     * GET /api/my-work/dashboard-summary?userId=X
     *
     * Lightweight summary for dashboard stat chips.
     *
     * pendingTasksCount logic:
     *   A task is PENDING when its status.sortOrder < MAX(sortOrder) for the project.
     *   The highest-order column is always treated as terminal (done),
     *   regardless of its name. Fully dynamic — unaffected by renames or reorders.
     *
     * Response: { "activeProjectCount": N, "pendingTasksCount": N }
     */
    @GetMapping("/dashboard-summary")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','GENERAL')")
    public ResponseEntity<Map<String, Long>> getDashboardSummary(@RequestParam Long userId) {
        MyWorkResponseDto data = myWorkService.getMyWork(userId);
        return ResponseEntity.ok(Map.of(
            "activeProjectCount", data.getActiveProjectCount(),
            "pendingTasksCount",  data.getPendingTasksCount()   // ← was totalTasksCount
        ));
    }
}