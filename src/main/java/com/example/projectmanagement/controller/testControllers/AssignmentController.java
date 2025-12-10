package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.*;
import com.example.projectmanagement.security.CurrentUser;
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
            @CurrentUser UserDto currentUser
    ) {
        // currentUserId could be logged in user — used for auditing (not strictly required here)
//        currentUser.getId()
//        Long currentUserId = prin == null ? null : Long.parseLong(principal.getName());
        AssignmentApplyResponse resp = assignmentService.applyAssignment(req, currentUser.getId());
        return ResponseEntity.ok(resp);
    }
}
