package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.dto.TaskCreateDto;
import com.example.projectmanagement.dto.TaskViewDto;
import com.example.projectmanagement.dto.TaskUpdateDto;
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
    TaskDto updateTaskStatus(Long taskId, Long statusId);
    Page<TaskViewDto> searchTasksView(String title, Task.Priority priority, Long assigneeId, Pageable pageable);
    TaskCreateDto updateTask(Long id, TaskUpdateDto dto);
    List<TaskViewDto> getTasksByStoryNew(Long storyId);
    List<TaskViewDto> getTasksByProjectId(Long projectId); // Added method
}
