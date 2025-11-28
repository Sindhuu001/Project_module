package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.testing.BugCreateRequest;
import com.example.projectmanagement.dto.testing.BugResponse;
import com.example.projectmanagement.dto.testing.BugStatusUpdateRequest;
import com.example.projectmanagement.service.BugService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/testing/bugs")
@RequiredArgsConstructor
public class BugController {

    private final BugService bugService;

    @PostMapping
    public ResponseEntity<BugResponse> createBug(
            @Valid @RequestBody BugCreateRequest req,
            Principal principal
    ) {
        Long reporter = principal == null ? null : Long.parseLong(principal.getName());
        BugResponse resp = bugService.createBug(req, reporter);
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/{bugId}/status")
    public ResponseEntity<BugResponse> updateStatus(
            @PathVariable Long bugId,
            @Valid @RequestBody BugStatusUpdateRequest req,
            Principal principal
    ) {
        Long userId = principal == null ? null : Long.parseLong(principal.getName());
        BugResponse resp = bugService.updateBugStatus(bugId, req, userId);
        return ResponseEntity.ok(resp);
    }
}
