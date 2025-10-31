package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.BugDto;
import com.example.projectmanagement.entity.Bug;
import com.example.projectmanagement.service.BugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bugs")
@CrossOrigin
public class BugController {

    @Autowired
    private BugService bugService;

    // ✅ Create new Bug
    @PostMapping
    public ResponseEntity<BugDto> createBug(@RequestBody BugDto bugDto) {
        BugDto createdBug = bugService.createBug(bugDto);
        return ResponseEntity.ok(createdBug);
    }

    // ✅ Get all Bugs
    @GetMapping
    public ResponseEntity<List<BugDto>> getAllBugs() {
        List<BugDto> bugs = bugService.getAllBugs();
        return ResponseEntity.ok(bugs);
    }

    // ✅ Get Bug by ID
    @GetMapping("/{id}")
    public ResponseEntity<BugDto> getBugById(@PathVariable Long id) {
        BugDto bug = bugService.getBugById(id);
        return ResponseEntity.ok(bug);
    }

    // ✅ Get Bugs by Project
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<BugDto>> getBugsByProject(@PathVariable Long projectId) {
        List<BugDto> bugs = bugService.getBugsByProject(projectId);
        return ResponseEntity.ok(bugs);
    }

    // ✅ Get Bugs by Assignee
    @GetMapping("/assignee/{userId}")
    public ResponseEntity<List<BugDto>> getBugsByAssignee(@PathVariable Long userId) {
        List<BugDto> bugs = bugService.getBugsByAssignee(userId);
        return ResponseEntity.ok(bugs);
    }

    // ✅ Get Bugs by Sprint
    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<List<BugDto>> getBugsBySprint(@PathVariable Long sprintId) {
        List<BugDto> bugs = bugService.getBugsBySprint(sprintId);
        return ResponseEntity.ok(bugs);
    }

    // ✅ Get Bugs by Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BugDto>> getBugsByStatus(@PathVariable Bug.Status status) {
        List<BugDto> bugs = bugService.getBugsByStatus(status);
        return ResponseEntity.ok(bugs);
    }

    // ✅ Get Bugs by Severity
    @GetMapping("/severity/{severity}")
    public ResponseEntity<List<BugDto>> getBugsBySeverity(@PathVariable Bug.Severity severity) {
        List<BugDto> bugs = bugService.getBugsBySeverity(severity);
        return ResponseEntity.ok(bugs);
    }

    // ✅ Update Bug (full update)
    @PutMapping("/{id}")
    public ResponseEntity<BugDto> updateBug(@PathVariable Long id, @RequestBody BugDto bugDto) {
        BugDto updatedBug = bugService.updateBug(id, bugDto);
        return ResponseEntity.ok(updatedBug);
    }

    // ✅ Partial update for Status only
    @PatchMapping("/{id}/status")
    public ResponseEntity<BugDto> updateBugStatus(@PathVariable Long id, @RequestParam Bug.Status status) {
        BugDto updatedBug = bugService.updateBugStatus(id, status);
        return ResponseEntity.ok(updatedBug);
    }

    // ✅ Delete Bug
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBug(@PathVariable Long id) {
        bugService.deleteBug(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Get Bugs by Epic
    @GetMapping("/epic/{epicId}")
    public ResponseEntity<List<BugDto>> getBugsByEpic(@PathVariable Long epicId) {
        List<BugDto> bugs = bugService.getBugsByEpic(epicId);
        return ResponseEntity.ok(bugs);
    }

    // ✅ Get Bugs by Task
    @GetMapping("/task/{taskId}")
    public ResponseEntity<List<BugDto>> getBugsByTask(@PathVariable Long taskId) {
        List<BugDto> bugs = bugService.getBugsByTask(taskId);
        return ResponseEntity.ok(bugs);
    }
}
