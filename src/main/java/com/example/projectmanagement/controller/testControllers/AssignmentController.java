package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.*;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// Spring Security Method Security
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;

import java.security.Principal;

@RestController
@RequestMapping("/api/test-execution/test-runs/assign")
@RequiredArgsConstructor

public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/validate")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<AssignmentValidateResponse> validate(
            @Valid @RequestBody AssignmentValidateRequest req
    ) {
        AssignmentValidateResponse resp = assignmentService.validateAssignment(req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
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
