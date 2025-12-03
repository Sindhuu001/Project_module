package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.testing.*;
import com.example.projectmanagement.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/test-execution/test-runs/assign")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/validate")
    public ResponseEntity<AssignmentValidateResponse> validate(
            @Valid @RequestBody AssignmentValidateRequest req
    ) {
        AssignmentValidateResponse resp = assignmentService.validateAssignment(req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/apply")
    public ResponseEntity<AssignmentApplyResponse> apply(
            @Valid @RequestBody AssignmentApplyRequest req,
            Principal principal
    ) {
        // currentUserId could be logged in user — used for auditing (not strictly required here)
        Long currentUserId = principal == null ? null : Long.parseLong(principal.getName());
        AssignmentApplyResponse resp = assignmentService.applyAssignment(req, currentUserId);
        return ResponseEntity.ok(resp);
    }
}
