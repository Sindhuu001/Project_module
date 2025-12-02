package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.BugCreateRequest;
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

import java.util.List;


@RestController
@RequestMapping("/api/testing/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;

    @PostMapping
    public ResponseEntity<BugResponse> createBug(
            @Valid @RequestBody BugCreateRequest req,
            @CurrentUser UserDto currentUser
    ) {
        BugResponse resp = bugService.createBug(req, currentUser.getId());
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{bugId}/status")
    public ResponseEntity<BugResponse> updateStatus(
            @PathVariable Long bugId,
            @Valid @RequestBody BugStatusUpdateRequest req,
            @CurrentUser UserDto currentUser
    ) {
        BugResponse resp = bugService.updateBugStatus(bugId, req, currentUser.getId());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<Page<BugResponse>> getBugsByProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<BugResponse> bugs = bugService.findBugsByProjectId(projectId, page, size);
        return ResponseEntity.ok(bugs);
    }

    @GetMapping("/projects/{projectId}/summaries")
    public ResponseEntity<List<BugSummaryResponse>> getBugSummariesByProject(
            @PathVariable Long projectId) {
        List<BugSummaryResponse> bugs = bugService.findBugSummariesByProjectId(projectId);
        return ResponseEntity.ok(bugs);
    }
}