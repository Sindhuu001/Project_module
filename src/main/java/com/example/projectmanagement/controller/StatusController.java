package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.StatusDto;
import com.example.projectmanagement.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

// Your custom security project package (for the User DTO and Custom Annotation)
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.dto.UserDto;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatusController {

    @Autowired
    private StatusService statusService;

    @PostMapping("/projects/{projectId}/statuses")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<StatusDto> addStatus(@PathVariable Long projectId, @RequestBody StatusDto statusDto) {
        return ResponseEntity.ok(statusService.addStatus(projectId, statusDto));
    }

    @GetMapping("/projects/{projectId}/statuses")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<StatusDto>> getStatusesByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(statusService.getStatusesByProject(projectId));
    }

    @DeleteMapping("/statuses/{statusId}")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<?> deleteStatus(@PathVariable Long statusId, @RequestParam(required = false) Long newStatusId) {
        statusService.deleteStatus(statusId, newStatusId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/statuses/reorder")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<StatusDto>> reorderStatuses(@RequestBody Map<Long, Integer> statusOrder) {
        return ResponseEntity.ok(statusService.reorderStatuses(statusOrder));
    }

    @PutMapping("/projects/{projectId}/statuses")
    @PreAuthorize("hasAnyRole('MANAGER','GENERAL')")
    public ResponseEntity<List<StatusDto>> syncStatuses(@PathVariable Long projectId, @RequestBody List<StatusDto> desiredStatuses) {
        return ResponseEntity.ok(statusService.syncStatuses(projectId, desiredStatuses));
    }
}
