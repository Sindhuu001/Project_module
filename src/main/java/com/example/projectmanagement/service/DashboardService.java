package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.DashboardSummaryDto;
import com.example.projectmanagement.entity.Epic;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final EpicRepository epicRepository;
    private final SprintRepository sprintRepository;
    
    private final StoryRepository storyRepository;

    public DashboardService(ProjectRepository projectRepository,
                            TaskRepository taskRepository,
                            EpicRepository epicRepository,
                            SprintRepository sprintRepository,
                            StoryRepository storyRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.epicRepository = epicRepository;
        this.sprintRepository = sprintRepository;
        this.storyRepository = storyRepository;
    }

    public DashboardSummaryDto getSummary() {
        long totalProjects = projectRepository.count();
        long totalTasks = taskRepository.count();
        long totalEpics = epicRepository.count();
        long totalStories = storyRepository.count();
        // Task status counts
        Map<Task.TaskStatus, Long> taskStatusMap = new EnumMap<>(Task.TaskStatus.class);
        for (Task.TaskStatus status : Task.TaskStatus.values()) {
            taskStatusMap.put(status, (long) taskRepository.findByStatus(status).size());
        }

        // Epic status counts
        Map<Epic.EpicStatus, Long> epicStatusMap = new EnumMap<>(Epic.EpicStatus.class);
        for (Epic.EpicStatus status : Epic.EpicStatus.values()) {
            epicStatusMap.put(status, (long) epicRepository.findByStatus(status).size());
        }

        // Story status counts
        Map<Story.StoryStatus, Long> storyStatusMap = new EnumMap<>(Story.StoryStatus.class);
        for (Story.StoryStatus status : Story.StoryStatus.values()) {
            storyStatusMap.put(status, (long) storyRepository.findByStatus(status).size());
        }

        // Convert enum maps to string-keyed maps for DTO
        Map<String, Long> taskStatusCount = new HashMap<>();
        taskStatusMap.forEach((key, value) -> taskStatusCount.put(key.name(), value));

        Map<String, Long> epicStatusCount = new HashMap<>();
        epicStatusMap.forEach((key, value) -> epicStatusCount.put(key.name(), value));

        Map<String, Long> storyStatusCount = new HashMap<>();
        storyStatusMap.forEach((key, value) -> storyStatusCount.put(key.name(), value));

        return DashboardSummaryDto.builder()
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .taskStatusCount(taskStatusCount)
                .totalEpics(totalEpics)
                .epicStatusCount(epicStatusCount)
                .totalStories(totalStories)
                .storyStatusCount(storyStatusCount)
                
                .build();
    }
    

    public Map<String, Long> getReminders() {
        Map<String, Long> reminders = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoDaysLater = now.plusDays(2);

        // 🔔 Tasks due in next 2 days
        reminders.put("taskDueSoonCount", taskRepository.countByDueDateBetween(now, twoDaysLater));

        // 📝 Tasks in TODO
        reminders.put("todoTaskCount", taskRepository.countByStatus(Task.TaskStatus.TODO));

        // 🚩 Projects with no owner
        reminders.put("unassignedProjectCount", projectRepository.countByOwnerIdIsNull());

        // 🕒 Sprints ending within next 2 days
        reminders.put("sprintsEndingSoonCount", sprintRepository.countByEndDateBetween(now, twoDaysLater));

        // 📘 Stories in TODO
        reminders.put("todoStoryCount", storyRepository.countByStatus(Story.StoryStatus.TODO));

        return reminders;
    }


    public Map<String, Object> getDashboardData(Long userId) {

    // --- Task Status Counts ---
    EnumMap<Task.TaskStatus, Long> taskStatusMap = new EnumMap<>(Task.TaskStatus.class);
    for (Task.TaskStatus status : Task.TaskStatus.values()) {
        taskStatusMap.put(status, taskRepository.countByAssigneeIdAndStatus(userId, status));
    }

    // --- Story Status Counts ---
    EnumMap<Story.StoryStatus, Long> storyStatusMap = new EnumMap<>(Story.StoryStatus.class);
    for (Story.StoryStatus status : Story.StoryStatus.values()) {
        storyStatusMap.put(status, storyRepository.countByAssigneeIdAndStatus(userId, status));
    }

    // --- Convert Enum keys to String for JSON output ---
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("taskStatus", taskStatusMap.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue)));
    // result.put("epicStatus", epicStatusMap.entrySet().stream()
    //         .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue)));
    result.put("storyStatus", storyStatusMap.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue)));

    return result;
}




}
