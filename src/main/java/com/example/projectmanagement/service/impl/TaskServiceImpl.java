package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.dto.*;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.repository.*;
import com.example.projectmanagement.service.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private StoryRepository storyRepository;
    @Autowired
    private SprintRepository sprintRepository;
    @Autowired
    private StatusRepository statusRepository;

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
    @Autowired
    private UserClient userClient;

    // ---------- CRUD Operations ----------

    @Override
    public long countTasksByStoryId(Long storyId) {
        return taskRepository.countByStoryId(storyId);
    }

    @Override
    public TaskCreateDto createTask(TaskCreateDto taskCreateDto) {
        Task task = new Task();

        // 1️⃣ Project validation (mandatory)
        Project project = projectRepository.findById(taskCreateDto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + taskCreateDto.getProjectId()));
        task.setProject(project);
        Set<Long> projectMembers = project.getMemberIds();
        Long projectOwnerId = project.getOwnerId();

        // 2️⃣ Status (optional)
        if (taskCreateDto.getStatusId() != null) {
            Status status = statusRepository.findById(taskCreateDto.getStatusId())
                    .orElseThrow(() -> new RuntimeException("Status not found"));
            task.setStatus(status);
        }

        // 3️⃣ Basic fields
        task.setId(taskCreateDto.getId());
        task.setTitle(taskCreateDto.getTitle());
        task.setDescription(taskCreateDto.getDescription());
        task.setPriority(taskCreateDto.getPriority());
        task.setStoryPoints(taskCreateDto.getStoryPoints());
        task.setDueDate(taskCreateDto.getDueDate());
        task.setBillable(taskCreateDto.isBillable());

        // 4️⃣ Assignee validation
        if (taskCreateDto.getAssigneeId() != null) {
            Long assigneeId = taskCreateDto.getAssigneeId();
            if (!projectMembers.contains(assigneeId) && !projectOwnerId.equals(assigneeId)) {
                throw new RuntimeException(
                        "Assignee ID " + assigneeId + " is not a member or owner of project " + project.getId());
            }
            task.setAssigneeId(assigneeId);
        }

        // 5️⃣ Reporter validation
        if (taskCreateDto.getReporterId() != null) {
            Long reporterId = taskCreateDto.getReporterId();
            if (!projectMembers.contains(reporterId) && !projectOwnerId.equals(reporterId)) {
                throw new RuntimeException(
                        "Reporter ID " + reporterId + " is not a member or owner of project " + project.getId());
            }
            task.setReporterId(reporterId);
        }

        // 6️⃣ Story assignment (optional)
        if (taskCreateDto.getStoryId() != null) {
            Story story = storyRepository.findById(taskCreateDto.getStoryId())
                    .orElseThrow(() -> new RuntimeException("Story not found with id: " + taskCreateDto.getStoryId()));
            task.setStory(story);
        }

        // 7️⃣ Sprint assignment logic
        if (taskCreateDto.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(taskCreateDto.getSprintId())
                    .orElseThrow(
                            () -> new RuntimeException("Sprint not found with id: " + taskCreateDto.getSprintId()));
            task.setSprint(sprint);
        } else if (task.getStory() != null && task.getStory().getSprint() != null) {
            task.setSprint(task.getStory().getSprint());
        }

        // 8️⃣ Save task
        Task saved = taskRepository.save(task);
        return mapToDto(saved);
    }

    @Override
    public TaskViewDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return mapToViewDto(task);
    }

    @Override
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        taskRepository.delete(task);
    }

    @Override
    public TaskCreateDto updateTask(Long id, TaskUpdateDto dto) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        Project project = existingTask.getProject();
        if (project == null)
            throw new RuntimeException("Task does not belong to any project");
        Set<Long> projectMembers = project.getMemberIds();
        Long projectOwnerId = project.getOwnerId();

        // Update fields
        if (dto.getTitle() != null)
            existingTask.setTitle(dto.getTitle());
        if (dto.getDescription() != null)
            existingTask.setDescription(dto.getDescription());
        if (dto.getPriority() != null)
            existingTask.setPriority(dto.getPriority());
        if (dto.getStoryPoints() != null)
            existingTask.setStoryPoints(dto.getStoryPoints());
        if (dto.getDueDate() != null)
            existingTask.setDueDate(dto.getDueDate());
        if (dto.getBillable() != null)
            existingTask.setBillable(dto.getBillable());

        // Reporter
        if (dto.getReporterId() != null) {
            Long reporterId = dto.getReporterId();
            if (!projectMembers.contains(reporterId) && !projectOwnerId.equals(reporterId)) {
                throw new RuntimeException(
                        "Reporter ID " + reporterId + " is not a member or owner of project " + project.getId());
            }
            existingTask.setReporterId(reporterId);
        }

        // Status
        if (dto.getStatusId() != null) {
            Status status = statusRepository.findById(dto.getStatusId())
                    .orElseThrow(() -> new RuntimeException("Status not found with id: " + dto.getStatusId()));
            existingTask.setStatus(status);
        }

        // Assignee
        if (dto.getAssigneeId() != null) {
            Long assigneeId = dto.getAssigneeId();
            if (!projectMembers.contains(assigneeId) && !projectOwnerId.equals(assigneeId)) {
                throw new RuntimeException(
                        "Assignee ID " + assigneeId + " is not a member or owner of project " + project.getId());
            }
            existingTask.setAssigneeId(assigneeId);
        } else
            existingTask.setAssigneeId(null);

        // Story
        if (dto.getStoryId() != null) {
            Story story = storyRepository.findById(dto.getStoryId())
                    .orElseThrow(() -> new RuntimeException("Story not found with id: " + dto.getStoryId()));
            existingTask.setStory(story);
        } else
            existingTask.setStory(null);

        // Sprint
        if (dto.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(dto.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + dto.getSprintId()));
            existingTask.setSprint(sprint);
        } else if (existingTask.getStory() != null && existingTask.getStory().getSprint() != null) {
            existingTask.setSprint(existingTask.getStory().getSprint());
        } else
            existingTask.setSprint(null);

        Task updatedTask = taskRepository.save(existingTask);
        return mapToDto(updatedTask);
    }

    // ---------- List & Summary ----------

    @Override
    public List<TaskDto> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TaskDto> getAllTasks(Pageable pageable) {
        return null;
    }

    @Override
    public List<TaskViewDto> getTasksByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream()
                .map(this::mapToViewDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskViewDto> getTasksBySprintId(Long projectId) {
        return taskRepository.findBySprintId(projectId).stream()
                .map(this::mapToViewDto)
                .collect(Collectors.toList());
    }

    // @Override
    // public Page<TaskDto> getAllTasks(Pageable pageable) {
    // return taskRepository.findAll(pageable).map(this::convertToDto);
    // }

    @Override
    public List<TaskDto.Summary> getTaskSummariesByProject(Long projectId) {
        List<TaskDto.Summary> summaries = taskRepository.findTaskSummariesByProjectId(projectId);
        List<UserDto> allUsers = userClient.findAll();
        Map<Long, UserDto> userMap = allUsers.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        for (TaskDto.Summary summary : summaries) {
            summary.setReporterName(summary.getReporterId() != null && userMap.containsKey(summary.getReporterId())
                    ? userMap.get(summary.getReporterId()).getName()
                    : "Unassigned");
            summary.setAssigneeName(summary.getAssigneeId() != null && userMap.containsKey(summary.getAssigneeId())
                    ? userMap.get(summary.getAssigneeId()).getName()
                    : "Unassigned");
        }
        return summaries;
    }

    @Override
    public List<TaskDto.Summary> getTaskSummariesBySprintId(Long sprintId) {
        return taskRepository.findTaskSummariesBySprintId(sprintId);
    }

    @Override
    public List<TaskDto> getTasksByStory(Long storyId) {
        return taskRepository.findByStoryId(storyId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskViewDto> getTasksByStoryNew(Long storyId) {
        return taskRepository.findByStoryId(storyId).stream()
                .map(this::mapToViewDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDto> getTasksByAssignee(Long assigneeId) {
        return taskRepository.findByAssigneeId(assigneeId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDto> getTasksByStatus(Long statusId) {
        return taskRepository.findByStatusId(statusId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDto> getBacklogTasks() {
        return taskRepository.findBacklogTasks().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskTimesheetDto> getTimesheetsTasksByAssignee(Long assigneeId) {

        return taskRepository.findByAssigneeId(assigneeId)
                .stream()
                .map(this::taskTimeConvertToDto)
                .collect(Collectors.toList());
    }

    // ---------- Search & Count ----------

    @Override
    public Page<TaskViewDto> searchTasksView(String title, Task.Priority priority, Long assigneeId, Pageable pageable) {
        Page<Task> tasks;
        if (assigneeId != null)
            tasks = taskRepository.findByAssigneeId(assigneeId, pageable);
        else if (title != null)
            tasks = taskRepository.findByTitleContaining(title, pageable);
        else if (priority != null)
            tasks = taskRepository.findByPriority(priority, pageable);
        else
            tasks = taskRepository.findAll(pageable);

        return tasks.map(this::mapToViewDto);
    }

    @Override
    public Page<TaskDto> searchTasks(String title, Task.Priority priority, Long assigneeId, Pageable pageable) {
        if (assigneeId != null)
            return taskRepository.findByAssigneeId(assigneeId, pageable).map(this::convertToDto);
        else if (title != null)
            return taskRepository.findByTitleContaining(title, pageable).map(this::convertToDto);
        else if (priority != null)
            return taskRepository.findByPriority(priority, pageable).map(this::convertToDto);
        else
            return taskRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public long countTasksByStatus(Long statusId) {
        return taskRepository.countByStatusId(statusId);
    }

    @Override
    public TaskStatusUpdateDto updateTaskStatus(Long taskId, Long statusId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + taskId));

        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Status not found with id: " + statusId));

        // Update task
        task.setStatus(status);
        taskRepository.save(task);

        // Update story status if task belongs to a story
        if (task.getStory() != null) {
            task.getStory().setStatus(status);
            storyRepository.save(task.getStory());
        }

        // Build clean response
        TaskStatusUpdateDto dto = new TaskStatusUpdateDto();
        dto.setId(task.getId());
        dto.setStatusId(status.getId());
        dto.setStatusName(status.getName());

        if (task.getStory() != null)
            dto.setStoryId(task.getStory().getId());

        if (task.getSprint() != null)
            dto.setSprintId(task.getSprint().getId());

        return dto;
    }

    // ---------- DTO Conversion ----------

    private TaskTimesheetDto taskTimeConvertToDto(Task task) {
        TaskTimesheetDto dto = modelMapper.map(task, TaskTimesheetDto.class);

        ProjectSmallDto projectDto = new ProjectSmallDto();
        projectDto.setId(task.getProject().getId());
        projectDto.setName(task.getProject().getName());

        dto.setProject(projectDto);

        return dto;
    }

    private TaskDto convertToDto(Task task) {
        TaskDto dto = modelMapper.map(task, TaskDto.class);

        Map<Long, UserDto> userMap = userClient.findAll().stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));

        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
            dto.setProject(projectService.convertToDto1(task.getProject(), userMap));
        }

        dto.setSprintId(task.getEffectiveSprintId());

        dto.setReporterId(task.getReporterId());
        dto.setReporter(task.getReporterId() != null ? userMap.get(task.getReporterId())
                : new UserDto(12345L, "Unknown User", "unknown.user@example.com", null));

        if (task.getStory() != null) {
            dto.setStoryId(task.getStory().getId());
            dto.setStory(storyService.convertToDto1(task.getStory(), userMap));

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

    private TaskCreateDto mapToDto(Task task) {
        TaskCreateDto dto = new TaskCreateDto();

        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority());
        dto.setStoryPoints(task.getStoryPoints());
        dto.setDueDate(task.getDueDate());
        dto.setBillable(task.isBillable());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        if (task.getStatus() != null)
            dto.setStatusId(task.getStatus().getId());
        if (task.getProject() != null)
            dto.setProjectId(task.getProject().getId());

        dto.setReporterId(task.getReporterId());
        dto.setAssigneeId(task.getAssigneeId());

        if (task.getStory() != null)
            dto.setStoryId(task.getStory().getId());
        if (task.getSprint() != null)
            dto.setSprintId(task.getSprint().getId());
        else if (task.getStory() != null && task.getStory().getSprint() != null)
            dto.setSprintId(task.getStory().getSprint().getId());
        else
            dto.setSprintId(null);

        return dto;
    }

    private TaskViewDto mapToViewDto(Task task) {
        TaskViewDto dto = new TaskViewDto();

        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority());
        dto.setStoryPoints(task.getStoryPoints());
        dto.setDueDate(task.getDueDate());
        dto.setBillable(task.isBillable());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());

        if (task.getStatus() != null) {
            dto.setStatusId(task.getStatus().getId());
            dto.setStatusName(task.getStatus().getName());
        }

        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
            dto.setProjectName(task.getProject().getName());
        }

        dto.setReporterId(task.getReporterId());
        dto.setAssigneeId(task.getAssigneeId());
        if (task.getReporterId() != null) {
            try {
                dto.setReporterName(userService.getUserWithRoles(task.getReporterId()).getName());
            } catch (Exception e) {
                dto.setReporterName("Unknown");
            }
        }
        if (task.getAssigneeId() != null) {
            try {
                dto.setAssigneeName(userService.getUserWithRoles(task.getAssigneeId()).getName());
            } catch (Exception e) {
                dto.setAssigneeName("Unassigned");
            }
        }

        if (task.getStory() != null) {
            Story story = task.getStory();
            dto.setStoryId(story.getId());
            dto.setStoryTitle(story.getTitle());
            if (story.getSprint() != null) {
                Sprint sprint = story.getSprint();
                dto.setSprintId(sprint.getId());
                dto.setSprintName(sprint.getName());
            }
        } else if (task.getSprint() != null) {
            Sprint sprint = task.getSprint();
            dto.setSprintId(sprint.getId());
            dto.setSprintName(sprint.getName());
        }

        return dto;
    }
}
