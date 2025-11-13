package com.example.projectmanagement.service;

import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.entity.Task.TaskStatus;
import com.example.projectmanagement.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    @Autowired private TaskRepository taskRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private StoryRepository storyRepository;
    @Autowired private SprintRepository sprintRepository;

    @Autowired private ModelMapper modelMapper;
    @Autowired private UserService userService;
    @Autowired private ProjectService projectService;
    @Autowired private SprintService sprintService;
    @Autowired private StoryService storyService;
    @Autowired private UserClient userClient;

    // ---------- CRUD Operations ----------

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
        task.setBillable(taskDto.isBillable());

        if (taskDto.getStoryId() != null) {
            Story story = storyRepository.findById(taskDto.getStoryId())
                    .orElseThrow(() -> new RuntimeException("Story not found with id: " + taskDto.getStoryId()));
            task.setStory(story);
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

        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setStatus(taskDto.getStatus());
        existingTask.setPriority(taskDto.getPriority());
        existingTask.setStoryPoints(taskDto.getStoryPoints());
        existingTask.setDueDate(taskDto.getDueDate());
        existingTask.setBillable(taskDto.isBillable());

        if (taskDto.getStoryId() != null) {
            Story story = storyRepository.findById(taskDto.getStoryId())
                    .orElseThrow(() -> new RuntimeException("Story not found with id: " + taskDto.getStoryId()));
            existingTask.setStory(story);
        } else {
            existingTask.setStory(null);
        }

        if (taskDto.getAssigneeId() != null) {
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

    public List<TaskDto.Summary> getTaskSummariesByProject(Long projectId) {
        List<TaskDto.Summary> summaries = taskRepository.findTaskSummariesByProjectId(projectId);

        // Fetch all users once instead of calling the API repeatedly
        List<UserDto> allUsers = userClient.findAll();
        Map<Long, UserDto> userMap = allUsers.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        for (TaskDto.Summary summary : summaries) {
            // Reporter name
            if (summary.getReporterId() != null && userMap.containsKey(summary.getReporterId())) {
                summary.setReporterName(userMap.get(summary.getReporterId()).getName());
            } else {
                summary.setReporterName("Unassigned");
            }

            // Assignee name
            if (summary.getAssigneeId() != null && userMap.containsKey(summary.getAssigneeId())) {
                summary.setAssigneeName(userMap.get(summary.getAssigneeId()).getName());
            } else {
                summary.setAssigneeName("Unassigned");
            }
        }
        return summaries;
    }


    public List<TaskDto.Summary> getTaskSummariesBySprintId(Long sprintId) {
        return taskRepository.findTaskSummariesBySprintId(sprintId);
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

   

    @Transactional
    public void updateTaskStatus(Long taskId, String newStatusStr) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        TaskStatus current = task.getStatus();

        TaskStatus next;
        try {
            next = TaskStatus.valueOf(newStatusStr); // parse string safely
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatusStr);
        }

        // Validate allowed transition
        boolean validTransition =
                (current == TaskStatus.BACKLOG && next == TaskStatus.TODO) ||
                (current == TaskStatus.TODO && next == TaskStatus.IN_PROGRESS) ||
                (current == TaskStatus.IN_PROGRESS && next == TaskStatus.DONE);

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "Invalid status transition: " + current + " → " + next
            );
        }

        task.setStatus(next);
        taskRepository.save(task);
    }


    // ---------- Search & Count ----------

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

    public long countTasksByStatus(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }

    // ---------- DTO Conversion ----------

    private TaskDto convertToDto(Task task) {
        TaskDto dto = modelMapper.map(task, TaskDto.class);

        List<UserDto> allUsers = userClient.findAll();
        Map<Long, UserDto> userMap = allUsers.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        dto.setProjectId(task.getProject().getId());
        dto.setSprintId(task.getSprintId());
        dto.setProject(task.getProject() != null ? projectService.convertToDto1(task.getProject(), userMap) : null);

        dto.setReporterId(task.getReporterId());
        dto.setReporter(task.getReporterId() != null
                ? userMap.get(task.getReporterId())
                : new UserDto(12345L, "Unknown User", "unknown.user@example.com", null));

        if (task.getStory() != null) {
            dto.setStoryId(task.getStory().getId());
            dto.setStory(storyService.convertToDto1(task.getStory(), userMap));

            // ✅ derive sprint via story
            if (task.getStory().getSprint() != null) {
                Sprint sprint = task.getStory().getSprint();
                dto.setSprintId(sprint.getId());
                dto.setSprint(sprintService.convertToDto1(sprint, userMap));
            }
        }

        if (task.getAssigneeId() != null) {
            dto.setAssigneeId(task.getAssigneeId());
            dto.setAssignee(userMap.get(task.getAssigneeId()));
        }

        dto.setBillable(task.isBillable());
        return dto;
    }
}
