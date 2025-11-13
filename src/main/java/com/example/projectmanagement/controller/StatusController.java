package com.example.projectmanagement.controller;

import com.example.projectmanagement.entity.Status;
import com.example.projectmanagement.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatusController {

    @Autowired
    private StatusService statusService;

    @PostMapping("/projects/{projectId}/statuses")
    public ResponseEntity<Status> addStatus(@PathVariable Long projectId, @RequestBody Status status) {
        return ResponseEntity.ok(statusService.addStatus(projectId, status));
    }

    @GetMapping("/projects/{projectId}/statuses")
    public ResponseEntity<List<Status>> getStatusesByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(statusService.getStatusesByProject(projectId));
    }

    @DeleteMapping("/statuses/{statusId}")
    public ResponseEntity<?> deleteStatus(@PathVariable Long statusId, @RequestParam(required = false) Long newStatusId) {
        statusService.deleteStatus(statusId, newStatusId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/statuses/reorder")
    public ResponseEntity<List<Status>> reorderStatuses(@RequestBody Map<Long, Integer> statusOrder) {
        return ResponseEntity.ok(statusService.reorderStatuses(statusOrder));
    }
}
