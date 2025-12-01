package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.BugCreateRequest;
import com.example.projectmanagement.dto.testing.BugResponse;
import com.example.projectmanagement.dto.testing.BugStatusUpdateRequest;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.BugService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
