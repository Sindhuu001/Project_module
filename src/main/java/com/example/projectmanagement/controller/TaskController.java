package com.example.projectmanagement.controller;

import com.example.projectmanagement.audit.annotation.AuditLog;
import com.example.projectmanagement.dto.*;
import com.example.projectmanagement.dto.testing.TaskResponse;
import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin
@AuditLog(entity = "Task")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // ------------------------------
    // Existing CRUD endpoints (untouched)
    // ------------------------------
    @PostMapping
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<TaskCreateDto> createTask(@Valid @RequestBody TaskCreateDto taskCreateDto) {
        TaskCreateDto createdTask = taskService.createTask(taskCreateDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<TaskViewDto> getTaskById(@PathVariable Long id) {
        TaskViewDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<Page<TaskViewDto>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Task.Priority priority,
            @RequestParam(required = false) Long assigneeId) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TaskViewDto> tasks = taskService.searchTasksView(title, priority, assigneeId, pageable);

        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager','Employee')")
    public ResponseEntity<TaskCreateDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateDto taskUpdateDto) {

        TaskCreateDto updatedTask = taskService.updateTask(id, taskUpdateDto);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/tasks")
    public ResponseEntity<List<TaskTimesheetDto>> getTasksByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(taskService.getTimesheetsTasksByAssignee(userId));
    }

    // ------------------------------
    // Refactored endpoints using TaskViewDto or TaskCreateDto
    // ------------------------------

    @GetMapping("/backlog")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<TaskViewDto>> getBacklogTasks() {
        List<TaskViewDto> tasks = taskService.getBacklogTasks().stream()
                .map(task -> taskService.getTaskById(task.getId()))
                .toList();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/status/{statusId}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<TaskViewDto>> getTasksByStatus(@PathVariable Long statusId) {
        List<TaskViewDto> tasks = taskService.getTasksByStatus(statusId).stream()
                .map(task -> taskService.getTaskById(task.getId()))
                .toList();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/status/{statusId}/count")
    public ResponseEntity<Long> getDoneTaskCount(@PathVariable Long statusId) {
        long count = taskService.countTasksByStatus(statusId);
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskStatusUpdateDto> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestBody Map<String, Long> payload) {

        Long statusId = payload.get("statusId");
        if (statusId == null) {
            return ResponseEntity.badRequest().build();
        }

        TaskStatusUpdateDto updated = taskService.updateTaskStatus(taskId, statusId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/story/{storyId}/count")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<?> getTaskCountByStory(@PathVariable Long storyId) {
        long count = taskService.countTasksByStoryId(storyId);
        return ResponseEntity.ok(Map.of("storyId", storyId, "taskCount", count));
    }

    @GetMapping("/assignee/{assigneeId}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<TaskViewDto>> getTasksByAssignee(@PathVariable Long assigneeId) {
        List<TaskViewDto> tasks = taskService.getTasksByAssignee(assigneeId).stream()
                .map(task -> taskService.getTaskById(task.getId()))
                .toList();
        return ResponseEntity.ok(tasks);
    }
    @PutMapping("/{taskId}/assign-story/{storyId}")
    public ResponseEntity<String> assignStoryToTask(
            @PathVariable Long taskId,
            @PathVariable Long storyId) {

        taskService.assignStory(taskId, storyId);
        return ResponseEntity.ok("Task attached to story successfully");
    }

    @PatchMapping("/{taskId}/assign-sprint")
    public ResponseEntity<TaskResponse> assignTaskToSprint(
            @PathVariable Long taskId,
            @RequestParam(required = false) Long sprintId) {

        return ResponseEntity.ok(taskService.assignTaskToSprint(taskId, sprintId));
    }



}
