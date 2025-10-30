package com.example.projectmanagement.controller;

import com.example.projectmanagement.ExternalDTO.ProjectIdName;
import com.example.projectmanagement.ExternalDTO.ProjectTasksDto;
import com.example.projectmanagement.dto.EpicDto;
import com.example.projectmanagement.dto.ProjectDto;
import com.example.projectmanagement.dto.SprintDto;
import com.example.projectmanagement.dto.StoryDto;
import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.entity.Project;
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
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

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
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectDto projectDto) {
        ProjectDto createdProject = projectService.createProject(projectDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        List<ProjectDto> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getProjectCount() {
        Long count = projectService.getProjectCount();
        return ResponseEntity.ok(count);
    }

    // ✅ GET project by ID
    @GetMapping("/{id}")
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
    public ResponseEntity<List<EpicDto>> getProjectEpics(@PathVariable Long id) {
        List<EpicDto> epics = epicService.getEpicsByProjectId(id);
        return ResponseEntity.ok(epics);
    }

    // ✅ GET Sprints by project ID
    @GetMapping("/{id}/sprints")
    public ResponseEntity<List<SprintDto>> getProjectSprints(@PathVariable Long id) {
        List<SprintDto> sprints = sprintService.getSprintsByProject(id);
        return ResponseEntity.ok(sprints);
    }

    // ✅ GET Tasks by project ID
    @GetMapping("/{id}/tasks")
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
    public ResponseEntity<List<Map<String, Object>>> getActiveProjectsByOwnerId(@PathVariable Long ownerId) {
        List<Map<String, Object>> activeProjects = projectService.getActiveProjectsByOwner1(ownerId);
        return ResponseEntity.ok(activeProjects);
    }

    // ✅ GET Projects by Member
    @GetMapping("/member/{userId}")
    public ResponseEntity<List<ProjectDto>> getProjectsByMember(@PathVariable Long userId) {
        List<ProjectDto> projects = projectService.getProjectsByMember(userId);
        return ResponseEntity.ok(projects);
    }

    // ✅ Add member to a project
    @PostMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectDto> addMemberToProject(@PathVariable Long projectId,
            @PathVariable Long userId) {
        ProjectDto updatedProject = projectService.addMemberToProject(projectId, userId);
        return ResponseEntity.ok(updatedProject);
    }

    // ✅ Remove member from project
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
}
