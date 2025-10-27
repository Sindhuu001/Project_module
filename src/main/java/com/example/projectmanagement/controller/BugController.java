package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.BugDto;
import com.example.projectmanagement.entity.Bug;
import com.example.projectmanagement.service.BugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bugs")
@CrossOrigin
public class BugController {

    @Autowired
    private BugService bugService;

    @PostMapping
    public BugDto createBug(@RequestBody BugDto bugDto) {
        return bugService.createBug(bugDto);
    }

    @GetMapping
    public List<BugDto> getAllBugs() {
        return bugService.getAllBugs();
    }

    @GetMapping("/{id}")
    public BugDto getBugById(@PathVariable Long id) {
        return bugService.getBugById(id);
    }

    @GetMapping("/project/{projectId}")
    public List<BugDto> getBugsByProject(@PathVariable Long projectId) {
        return bugService.getBugsByProject(projectId);
    }

    @GetMapping("/assignee/{userId}")
    public List<BugDto> getBugsByAssignee(@PathVariable Long userId) {
        return bugService.getBugsByAssignee(userId);
    }

    @GetMapping("/sprint/{sprintId}")
    public List<BugDto> getBugsBySprint(@PathVariable Long sprintId) {
        return bugService.getBugsBySprint(sprintId);
    }

    @GetMapping("/status/{status}")
    public List<BugDto> getBugsByStatus(@PathVariable Bug.Status status) {
        return bugService.getBugsByStatus(status);
    }

    @GetMapping("/severity/{severity}")
    public List<BugDto> getBugsBySeverity(@PathVariable Bug.Severity severity) {
        return bugService.getBugsBySeverity(severity);
    }

    @PutMapping("/{id}")
    public BugDto updateBug(@PathVariable Long id, @RequestBody BugDto bugDto) {
        return bugService.updateBug(id, bugDto);
    }

    @PatchMapping("/{id}/status")
    public BugDto updateBugStatus(@PathVariable Long id, @RequestParam Bug.Status status) {
        return bugService.updateBugStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public void deleteBug(@PathVariable Long id) {
        bugService.deleteBug(id);
    }

    @GetMapping("/epic/{epicId}")
    public List<BugDto> getBugsByEpic(@PathVariable Long epicId) {
        return bugService.getBugsByEpic(epicId);
    }

    @GetMapping("/task/{taskId}")
    public List<BugDto> getBugsByTask(@PathVariable Long taskId) {
        return bugService.getBugsByTask(taskId);
    }

}
