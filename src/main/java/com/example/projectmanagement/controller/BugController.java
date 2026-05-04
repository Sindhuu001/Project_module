package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.BugAssignRequest;
import com.example.projectmanagement.dto.testing.BugCreateRequest;
import com.example.projectmanagement.dto.testing.BugDetailResponse;
import com.example.projectmanagement.dto.testing.BugResponse;
import com.example.projectmanagement.dto.testing.BugStatusUpdateRequest;
import com.example.projectmanagement.dto.testing.BugSummaryResponse;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.BugService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;

import java.util.List;


@RestController
@RequestMapping("/api/testing/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<BugResponse> createBug(
            @Valid @RequestBody BugCreateRequest req,
            @CurrentUser UserDto currentUser
    ) {
        BugResponse resp = bugService.createBug(req, currentUser.getId());
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{bugId}/status")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<BugResponse> updateStatus(
            @PathVariable Long bugId,
            @Valid @RequestBody BugStatusUpdateRequest req,
            @CurrentUser UserDto currentUser
    ) {
        BugResponse resp = bugService.updateBugStatus(bugId, req, currentUser.getId());
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{bugId}/assign")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<BugResponse> assignBug(
            @PathVariable Long bugId,
            @Valid @RequestBody BugAssignRequest req
    ) {
        BugResponse resp = bugService.assignBug(bugId, req);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{bugId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<BugDetailResponse> getBugById(@PathVariable Long bugId) {
        BugDetailResponse bug = bugService.getBugById(bugId);
        return ResponseEntity.ok(bug);
    }

    @GetMapping("/projects/{projectId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<Page<BugResponse>> getBugsByProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<BugResponse> bugs = bugService.findBugsByProjectId(projectId, page, size);
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/projects/{projectId}/summaries")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<List<BugSummaryResponse>> getBugSummariesByProject(
            @PathVariable Long projectId) {
        List<BugSummaryResponse> bugs = bugService.findBugSummariesByProjectId(projectId);
        return ResponseEntity.ok(bugs);
    }
}
