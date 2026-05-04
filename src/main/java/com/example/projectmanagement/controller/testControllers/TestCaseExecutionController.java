package com.example.projectmanagement.controller.testControllers;

import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.dto.testing.TestCaseExecutionRequest;
import com.example.projectmanagement.dto.testing.TestCaseExecutionResponse;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.TestCaseExecutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;

@RestController
@RequestMapping("/api/test-execution/cases")
@RequiredArgsConstructor
public class TestCaseExecutionController {

    private final TestCaseExecutionService service;

    @PostMapping("/pass")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestCaseExecutionResponse> passCase(
            @Valid @RequestBody TestCaseExecutionRequest req,
            @CurrentUser UserDto currentUser
    ) {
        return ResponseEntity.ok(service.passCase(req, currentUser.getId()));
    }

    @PostMapping("/fail")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestCaseExecutionResponse> failCase(
            @Valid @RequestBody TestCaseExecutionRequest req,
            @CurrentUser UserDto currentUser
    ) {
        return ResponseEntity.ok(service.failCase(req, currentUser.getId()));
    }

    @PostMapping("/block")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestCaseExecutionResponse> blockCase(
            @Valid @RequestBody TestCaseExecutionRequest req,
            @CurrentUser UserDto currentUser
    ) {
        return ResponseEntity.ok(service.blockCase(req, currentUser.getId()));
    }

    @PostMapping("/skip")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')") // Only allow MANAGER and GENERAL roles to access this endpoint
    public ResponseEntity<TestCaseExecutionResponse> skipCase(
            @Valid @RequestBody TestCaseExecutionRequest req,
            @CurrentUser UserDto currentUser
    ) {
        return ResponseEntity.ok(service.skipCase(req, currentUser.getId()));
    }
}
