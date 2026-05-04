package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.MyWorkResponseDto;
import com.example.projectmanagement.service.MyWorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;


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
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<MyWorkResponseDto> getMyWork(@RequestParam Long userId) {
        return ResponseEntity.ok(myWorkService.getMyWork(userId));
    }

    /**
     * Completed items — fetched lazily only when user expands the Completed
     * section.
     * Separate endpoint keeps the main payload lean.
     */
    @GetMapping("/completed")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<MyWorkResponseDto> getMyWorkCompleted(@RequestParam Long userId) {
        return ResponseEntity.ok(myWorkService.getMyWorkCompleted(userId));
    }
}