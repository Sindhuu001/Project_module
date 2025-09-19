// 📁 Updated TaskService.java with Role-Based Access Check
package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.repository.*;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    @Autowired private TaskRepository taskRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private StoryRepository storyRepository;
    @Autowired private SprintRepository sprintRepository;
    
    @Autowired 
    private ModelMapper modelMapper;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private StoryService storyService;

    public long countTasksByStoryId(Long storyId) {
        return taskRepository.countByStoryId(storyId);
    }

    public TaskDto createTask(TaskDto taskDto) {
        Project project = projectRepository.findById(taskDto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + taskDto.getProjectId()));

        UserDto reporter = userService.getUserWithRoles(taskDto.getReporterId());
                
        Task task = modelMapper.map(taskDto, Task.class);
        task.setProject(project);
        task.setReporterId(reporter.getId());

        if (taskDto.getStoryId() != null) {
            Story story = storyRepository.findById(taskDto.getStoryId())
                    .orElseThrow(() -> new RuntimeException("Story not found with id: " + taskDto.getStoryId()));
            task.setStory(story);
        }

        if (taskDto.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(taskDto.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + taskDto.getSprintId()));
            task.setSprint(sprint);
        }

        if (taskDto.getAssigneeId() != null) {
            UserDto assignee = userService.getUserWithRoles(taskDto.getAssigneeId());
                    
            task.setAssigneeId(assignee.getId());
        }

        Task savedTask = taskRepository.save(task);
        return convertToDto(savedTask);
    }

    public TaskDto updateTask(Long id, TaskDto taskDto) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        UserDto currentUser = userService.getUserWithRoles(taskDto.getReporterId());
                

        if (!RolePermissionChecker.canUpdateTask(currentUser.getRoles())) {
            throw new RuntimeException("Access denied: You are not allowed to update tasks.");
        }

        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setStatus(taskDto.getStatus());
        existingTask.setPriority(taskDto.getPriority());
        existingTask.setStoryPoints(taskDto.getStoryPoints());
        existingTask.setDueDate(taskDto.getDueDate());

        if (taskDto.getStoryId() != null) {
            Story story = storyRepository.findById(taskDto.getStoryId())
                    .orElseThrow(() -> new RuntimeException("Story not found with id: " + taskDto.getStoryId()));
            existingTask.setStory(story);
        } else {
            existingTask.setStory(null);
        }

        if (taskDto.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(taskDto.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + taskDto.getSprintId()));
            existingTask.setSprint(sprint);
        } else {
            existingTask.setSprint(null);
        }

        if (taskDto.getAssigneeId() != null) {
            // UserDto assignee = userClient.findById(taskDto.getAssigneeId());
                    
            existingTask.setAssigneeId(taskDto.getAssigneeId());
        } else {
            existingTask.setAssigneeId(null);
        }

        Task updatedTask = taskRepository.save(existingTask);
        return convertToDto(updatedTask);
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new RuntimeException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    public TaskDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return convertToDto(task);
    }

    public List<TaskDto> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Page<TaskDto> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public List<TaskDto> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksBySprint(Long sprintId) {
        return taskRepository.findBySprintId(sprintId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByStory(Long storyId) {
        return taskRepository.findByStoryId(storyId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByAssignee(Long assigneeId) {
        return taskRepository.findByAssigneeId(assigneeId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getTasksByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TaskDto> getBacklogTasks() {
        return taskRepository.findBacklogTasks().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public TaskDto assignTaskToSprint(Long taskId, Long sprintId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + sprintId));

        task.setSprint(sprint);
        Task updatedTask = taskRepository.save(task);
        return convertToDto(updatedTask);
    }

    public TaskDto removeTaskFromSprint(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        task.setSprint(null);
        Task updatedTask = taskRepository.save(task);
        return convertToDto(updatedTask);
    }

    public Page<TaskDto> searchTasks(String title, Task.Priority priority, Long assigneeId, Pageable pageable) {
        if (assigneeId != null) {
            return taskRepository.findByAssigneeId(assigneeId, pageable)
                    .map(this::convertToDto);
        } else if (title != null) {
            return taskRepository.findByTitleContaining(title, pageable)
                    .map(this::convertToDto);
        } else if (priority != null) {
            return taskRepository.findByPriority(priority, pageable)
                    .map(this::convertToDto);
        } else {
            return taskRepository.findAll(pageable)
                    .map(this::convertToDto);
        }
    }

    private TaskDto convertToDto(Task task) {
        TaskDto dto = modelMapper.map(task, TaskDto.class);
        dto.setProjectId(task.getProject().getId());
        dto.setProject(task.getProject() != null ? projectService.convertToDto(task.getProject()) : null);
        dto.setReporterId(task.getReporterId());
        dto.setReporter(task.getReporterId() != null ? userService.getUserWithRoles(task.getReporterId()) : new UserDto(12345L, "Unknown User", "unknown.user@example.com", null));
        if (task.getStory() != null) {
            dto.setStoryId(task.getStory().getId());
            dto.setStory(storyService.convertToDto(task.getStory()));
        }
        if (task.getSprint() != null) {
            dto.setSprintId(task.getSprint().getId());
            dto.setSprint(sprintService.convertToDto(task.getSprint()));
        }
        if (task.getAssigneeId() != null) {
            dto.setAssigneeId(task.getAssigneeId());
            dto.setAssignee(userService.getUserWithRoles(task.getAssigneeId()));
        }
        return dto;
    }
}