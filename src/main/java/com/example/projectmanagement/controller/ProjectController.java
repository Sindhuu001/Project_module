package com.example.projectmanagement.controller;

import com.example.projectmanagement.ExternalDTO.ProjectIdName;
import com.example.projectmanagement.ExternalDTO.ProjectTasksDto;
import com.example.projectmanagement.dto.EpicDto;
import com.example.projectmanagement.dto.ProjectDto;
import com.example.projectmanagement.dto.SprintDto;
import com.example.projectmanagement.dto.StoryDto;
import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.security.CurrentUser;
import com.example.projectmanagement.service.EpicService;
import com.example.projectmanagement.service.ProjectService;
import com.example.projectmanagement.service.SprintService;
import com.example.projectmanagement.service.StoryService;
import com.example.projectmanagement.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EpicService epicService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private StoryService storyService;

    // ✅ CREATE a new project
    
    @PostMapping
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectDto projectDto) {
        ProjectDto createdProject = projectService.createProject(projectDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        long start = System.currentTimeMillis();
        List<ProjectDto> projects = projectService.getAllProjects();
        System.out.println("*****************Time taken to fetch all projects: " + (System.currentTimeMillis() - start) + " ms");
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getProjectCount() {
        Long count = projectService.getProjectCount();
        return ResponseEntity.ok(count);
    }

    // ✅ GET project by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
        ProjectDto project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    // ✅ UPDATE project
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long id,
            @RequestBody ProjectDto updatedProjectDto) {
        ProjectDto updated = projectService.updateProject(id, updatedProjectDto);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/api/projects/{projectId}/unarchive")
    public ResponseEntity<ProjectDto> unarchiveProject(@PathVariable Long projectId) {
        ProjectDto dto = projectService.unarchiveProject(projectId);
        return ResponseEntity.ok(dto);
    }

    // ✅ DELETE project
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ GET all projects with pagination, filters
    // @GetMapping
    // public ResponseEntity<Page<ProjectDto>> getAllProjects(
    // @RequestParam(defaultValue = "0") int page,
    // @RequestParam(defaultValue = "10") int size,
    // @RequestParam(defaultValue = "id") String sortBy,
    // @RequestParam(defaultValue = "asc") String sortDir,
    // @RequestParam(required = false) String name,
    // @RequestParam(required = false) Project.ProjectStatus status) {

    // Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() :
    // Sort.by(sortBy).ascending();
    // Pageable pageable = PageRequest.of(page, size, sort);

    // Page<ProjectDto> projects = projectService.searchProjects(name, status,
    // pageable);
    // return ResponseEntity.ok(projects);
    // }

    // ✅ GET Epics by project ID
    @GetMapping("/{id}/epics")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<EpicDto>> getProjectEpics(@PathVariable Long id) {
        List<EpicDto> epics = epicService.getEpicsByProjectId(id);
        return ResponseEntity.ok(epics);
    }

    // ✅ GET Sprints by project ID
    @GetMapping("/{id}/sprints")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<SprintDto>> getProjectSprints(@PathVariable Long id) {
        List<SprintDto> sprints = sprintService.getSprintsByProject(id);
        return ResponseEntity.ok(sprints);
    }

    // ✅ GET Tasks by project ID
    @GetMapping("/{id}/tasks")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<TaskDto>> getProjectTasks(@PathVariable Long id) {
        List<TaskDto> tasks = taskService.getTasksByProject(id);
        return ResponseEntity.ok(tasks);
    }

    // ✅ GET Projects by Owner
    @GetMapping("/owner")
    public ResponseEntity<List<ProjectDto>> getProjectsByOwner(@CurrentUser UserDto currentUser) {
        System.out.println("******Current User:******** " + currentUser.getName() + ", Roles: " + currentUser.getRoles());
        List<ProjectDto> projects = projectService.getProjectsByOwner(currentUser.getId());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<Map<String, Object>>> getActiveProjectsByOwnerId(@PathVariable Long ownerId) {
        List<Map<String, Object>> activeProjects = projectService.getActiveProjectsByOwner1(ownerId);
        return ResponseEntity.ok(activeProjects);
    }

    // ✅ GET Projects by Member
    @GetMapping("/member/{userId}")
    @PreAuthorize("hasAnyRole('Manager','Admin','Employee')")
    public ResponseEntity<List<ProjectDto>> getProjectsByMember(@PathVariable Long userId) {
        List<ProjectDto> projects = projectService.getProjectsByMember(userId);
        return ResponseEntity.ok(projects);
    }

    // ✅ Add member to a project
    @PutMapping("/{projectId}/members/{userId}")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<ProjectDto> addMemberToProject(@PathVariable Long projectId,
            @PathVariable Long userId) {
        ProjectDto updatedProject = projectService.addMemberToProject(projectId, userId);
        return ResponseEntity.ok(updatedProject);
    }

    // ✅ Remove member from project
    @PreAuthorize("hasRole('Manager')")
    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectDto> removeMemberFromProject(@PathVariable Long projectId,
            @PathVariable Long userId) {
        ProjectDto updatedProject = projectService.removeMemberFromProject(projectId, userId);
        return ResponseEntity.ok(updatedProject);
    }

    @GetMapping("/{projectId}/stories")
    public ResponseEntity<List<StoryDto>> getStoriesByProject(@PathVariable Long projectId) {
        List<StoryDto> stories = storyService.getStoriesByProjectId(projectId);
        return ResponseEntity.ok(stories);
    }

    @GetMapping("/projects-tasks")
    public List<ProjectTasksDto> getProjectsWithTasks() {
        return projectService.getAllProjectsWithTasks();
    }

    @GetMapping("/get_project_info")
    public ResponseEntity<List<ProjectIdName>> getAllProjectInfo() {
        List<ProjectIdName> projects = projectService.getAllProjectInfo();
        if (projects.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/member/{userId}/active-projects")
    public ResponseEntity<List<ProjectIdName>> getActiveProjectsByMember(@PathVariable Long userId) {
        List<ProjectIdName> projects = projectService.getActiveProjectsByMember(userId);
        if (projects.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(projects);
    }

    @GetMapping("{id}/members")
    public ResponseEntity<List<UserDto>> getProjectMembers(@PathVariable Long id) {
        List<UserDto> members = projectService.getProjectMembers(id);


        // if (members.isEmpty()) {
        // return ResponseEntity.noContent().build();
        // }
        return ResponseEntity.ok(members);
    }
  @GetMapping("{id}/members-with-owner")
public ResponseEntity<List<UserDto>> getProjectMembersWithOwner(@PathVariable Long id) {
    // Get project members
    List<UserDto> members = projectService.getProjectMembers(id);

    // Get project owner
    UserDto owner = projectService.getProjectOwner(id);

    // Combine both in a single list (owner first)
    List<UserDto> combined = new ArrayList<>();
    if (owner != null) {
        combined.add(owner);
    }
    if (members != null && !members.isEmpty()) {
        // Avoid duplicate if owner is also listed as a member
        combined.addAll(
            members.stream()
                   .filter(m -> !m.getId().equals(owner.getId()))
                   .collect(Collectors.toList())
        );
    }

    return ResponseEntity.ok(combined);
}


    
}
