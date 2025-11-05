package com.example.projectmanagement.controller;
import com.example.projectmanagement.dto.StoryDto;
import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.service.StoryService;
import com.example.projectmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
 
@RestController

@RequestMapping("/api/stories")

@CrossOrigin 

public class StoryController {
 
    @Autowired
    private StoryService storyService;
 
    @Autowired
    private TaskService taskService;
 
    // ✅ Create a story

    @PostMapping
   // @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<StoryDto> createStory(@Valid @RequestBody StoryDto storyDto) {
        StoryDto createdStory = storyService.createStory(storyDto);
        return new ResponseEntity<>(createdStory, HttpStatus.CREATED);
    }

    @GetMapping("/no-epic")
    public ResponseEntity<List<StoryDto>> getStoriesWithoutEpic(@RequestParam Long projectId) {
        List<StoryDto> stories = storyService.getStoriesWithoutEpic(projectId);
        return ResponseEntity.ok(stories);
    }

 
    // ✅ Get story by ID

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<StoryDto> getStoryById(@PathVariable Long id) {
        StoryDto story = storyService.getStoryById(id);
        return ResponseEntity.ok(story);
    }
    
    // ✅ Get all stories with optional filters and pagination

    @GetMapping
   // @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<Page<StoryDto>> getAllStories(

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

                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
 
        Page<StoryDto> stories = storyService.searchStories(

                title, priority, epicId, projectId, sprintId, pageable

        );

        return ResponseEntity.ok(stories);

    }
 
    // ✅ Get all tasks under a story

    @GetMapping("/{id}/tasks")
   // @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<TaskDto>> getStoryTasks(@PathVariable Long id) {

        List<TaskDto> tasks = taskService.getTasksByStory(id);

        return ResponseEntity.ok(tasks);

    }
 
    // ✅ Get stories by status

    @GetMapping("/status/{status}")
   // @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<StoryDto>> getStoriesByStatus(@PathVariable Story.StoryStatus status) {

        List<StoryDto> stories = storyService.getStoriesByStatus(status);

        return ResponseEntity.ok(stories);

    }
 
    // ✅ Get stories by assignee ID

    @GetMapping("/assignee/{assigneeId}")
   // @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<StoryDto>> getStoriesByAssignee(@PathVariable Long assigneeId) {

        List<StoryDto> stories = storyService.getStoriesByAssignee(assigneeId);

        return ResponseEntity.ok(stories);

    }
    @GetMapping("/epic/{epicId}")
   // @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<StoryDto>> getStoriesByEpic(@PathVariable Long epicId) {
        List<StoryDto> stories = storyService.getStoriesByEpic(epicId);
        return ResponseEntity.ok(stories);
    }
 
    // ✅ Update a story

    @PutMapping("/{id}")
   // @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<StoryDto> updateStory(@PathVariable Long id, @Valid @RequestBody StoryDto storyDto) {

        StoryDto updatedStory = storyService.updateStory(id, storyDto);

        return ResponseEntity.ok(updatedStory);

    }
 
    // ✅ Delete a story

    @DeleteMapping("/{id}")
   // @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<Void> deleteStory(@PathVariable Long id) {

        storyService.deleteStory(id);

        return ResponseEntity.noContent().build();

    }
    @GetMapping("/sprint/{sprintId}")
    public ResponseEntity<List<StoryDto>> getStoriesBySprint(@PathVariable Long sprintId) {
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

 