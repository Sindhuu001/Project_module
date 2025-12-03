package com.example.projectmanagement.controller;
import com.example.projectmanagement.dto.StoryDto;
import com.example.projectmanagement.audit.annotation.AuditLog;
import com.example.projectmanagement.dto.StoryCreateDto;
import com.example.projectmanagement.dto.StoryViewDto;
import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.dto.TaskViewDto;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.service.StoryService;
import com.example.projectmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
 
@RestController
@AuditLog(entity = "Story")
@RequestMapping("/api/stories")
@CrossOrigin 
public class StoryController {
 
    @Autowired
    private StoryService storyService;
 
    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<StoryCreateDto> createStory(@Valid @RequestBody StoryCreateDto dto) {
        StoryCreateDto created = storyService.createStory(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/no-epic")
    public ResponseEntity<List<StoryDto>> getStoriesWithoutEpic(@RequestParam Long projectId) {
        List<StoryDto> stories = storyService.getStoriesWithoutEpic(projectId);
        return ResponseEntity.ok(stories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoryViewDto> getStoryById(@PathVariable Long id) {
        StoryViewDto story = storyService.getStoryViewById(id);
        return ResponseEntity.ok(story);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<Page<StoryViewDto>> getAllStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Story.Priority priority,
            @RequestParam(required = false) Long epicId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long sprintId
    ) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StoryViewDto> stories = storyService.searchStoriesView(
                title, priority, epicId, projectId, sprintId, pageable
        );

        return ResponseEntity.ok(stories);
    }


    @GetMapping("/{id}/tasks")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<TaskViewDto>> getStoryTasks(@PathVariable Long id) {
        List<TaskViewDto> tasks = taskService.getTasksByStoryNew(id);
        return ResponseEntity.ok(tasks);
    }
 
    @GetMapping("/status/{statusId}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<StoryViewDto>> getStoriesByStatus(@PathVariable Long statusId) {
        List<StoryViewDto> stories = storyService.getStoriesByStatus(statusId);
        return ResponseEntity.ok(stories);
    }
 
    @GetMapping("/assignee/{assigneeId}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<StoryDto>> getStoriesByAssignee(@PathVariable Long assigneeId) {
        List<StoryDto> stories = storyService.getStoriesByAssignee(assigneeId);
        return ResponseEntity.ok(stories);
    }

    @GetMapping("/epic/{epicId}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<StoryViewDto>> getStoriesByEpic(@PathVariable Long epicId) {
        List<StoryViewDto> stories = storyService.getStoriesByEpic(epicId);
        return ResponseEntity.ok(stories);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<StoryCreateDto> updateStory(
            @PathVariable Long id,
            @Valid @RequestBody StoryCreateDto storyCreateDto) {

        StoryCreateDto updated = storyService.updateStory(id, storyCreateDto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{storyId}/status")
    @PreAuthorize("hasAnyRole('Manager','Employee')")
    public ResponseEntity<StoryDto> updateStoryStatus(@PathVariable Long storyId, @RequestBody Map<String, Long> payload) {
        Long statusId = payload.get("statusId");
        if (statusId == null) {
            return ResponseEntity.badRequest().build();
        }
        StoryDto updatedStory = storyService.updateStoryStatus(storyId, statusId);
        return ResponseEntity.ok(updatedStory);
    }
 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {
        storyService.deleteStory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<List<StoryViewDto>> getStoriesBySprint(@PathVariable Long sprintId) {
        return ResponseEntity.ok(storyService.getStoriesBySprint(sprintId));
    }

    @PutMapping("/{storyId}/assign-sprint")
    public ResponseEntity<String> assignStoryToSprint(
            @PathVariable Long storyId,
            @RequestBody(required = false) Map<String, Long> request) {
        Long sprintId = request != null ? request.get("sprintId") : null;
        storyService.assignStoryToSprint(storyId, sprintId);
        return ResponseEntity.ok("Sprint assignment updated successfully.");
    }
}
