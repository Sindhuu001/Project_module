package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.*;
import com.example.projectmanagement.dto.testing.TaskResponse;
import com.example.projectmanagement.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    long countTasksByStoryId(Long storyId);

    TaskCreateDto createTask(TaskCreateDto taskCreateDto);

    TaskViewDto getTaskById(Long id);

    void deleteTask(Long id);

    List<TaskDto> getAllTasks();

    Page<TaskDto> getAllTasks(Pageable pageable);

    List<TaskDto.Summary> getTaskSummariesByProject(Long projectId);

    List<TaskDto.Summary> getTaskSummariesBySprintId(Long sprintId);

    List<TaskDto> getTasksByStory(Long storyId);

    List<TaskDto> getTasksByAssignee(Long assigneeId);

    List<TaskDto> getTasksByStatus(Long statusId);

    List<TaskDto> getBacklogTasks();

    Page<TaskDto> searchTasks(String title, Task.Priority priority, Long assigneeId, Pageable pageable);

    long countTasksByStatus(Long statusId);

    TaskStatusUpdateDto updateTaskStatus(Long taskId, Long statusId);

    Page<TaskViewDto> searchTasksView(String title, Task.Priority priority, Long assigneeId, Pageable pageable);

    TaskCreateDto updateTask(Long id, TaskUpdateDto dto);

    List<TaskViewDto> getTasksByStoryNew(Long storyId);

    List<TaskTimesheetDto> getTimesheetsTasksByAssignee(Long assigneeId);

    List<TaskViewDto> getTasksByProjectId(Long projectId);

    List<TaskViewDto> getTasksBySprintId(Long projectId);

    void assignStory(Long taskId, Long storyId);

    TaskResponse assignTaskToSprint(Long taskId, Long sprintId);
}
