package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.TaskDto;
import com.example.projectmanagement.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    long countTasksByStoryId(Long storyId);
    TaskDto createTask(TaskDto taskDto);
    TaskDto updateTask(Long id, TaskDto taskDto);
    void deleteTask(Long id);
    TaskDto getTaskById(Long id);
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
}
