package com.example.projectmanagement.controller;

import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*") 
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto taskDto) {
        TaskDto createdTask = taskService.createTask(taskDto);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        TaskDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }
    
    @GetMapping
    public ResponseEntity<Page<TaskDto>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Task.Priority priority,
            @RequestParam(required = false) Long assigneeId) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<TaskDto> tasks = taskService.searchTasks(title, priority, assigneeId, pageable);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/backlog")
    public ResponseEntity<List<TaskDto>> getBacklogTasks() {
        List<TaskDto> tasks = taskService.getBacklogTasks();
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskDto>> getTasksByStatus(@PathVariable Task.TaskStatus status) {
        List<TaskDto> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDto taskDto) {
        TaskDto updatedTask = taskService.updateTask(id, taskDto);
        return ResponseEntity.ok(updatedTask);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
   }

  

    @GetMapping("/story/{storyId}/count")
    public ResponseEntity<?> getTaskCountByStory(@PathVariable Long storyId) {
        long count = taskService.countTasksByStoryId(storyId);
        return ResponseEntity.ok(Map.of("storyId", storyId, "taskCount", count));
    }
    @GetMapping("/assignee/{assigneeId}")
    public ResponseEntity<List<TaskDto>> getTasksByAssignee(@PathVariable Long assigneeId) {
        List<TaskDto> tasks = taskService.getTasksByAssignee(assigneeId);
        return ResponseEntity.ok(tasks);
    }
}
