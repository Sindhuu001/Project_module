package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.DashboardSummaryDto;
import com.example.projectmanagement.entity.Epic;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.entity.Status;
import com.example.projectmanagement.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final EpicRepository epicRepository;
    private final SprintRepository sprintRepository;
    private final StoryRepository storyRepository;
    private final StatusRepository statusRepository;

    public DashboardService(ProjectRepository projectRepository,
                            TaskRepository taskRepository,
                            EpicRepository epicRepository,
                            SprintRepository sprintRepository,
                            StoryRepository storyRepository,
                            StatusRepository statusRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.epicRepository = epicRepository;
        this.sprintRepository = sprintRepository;
        this.storyRepository = storyRepository;
        this.statusRepository = statusRepository;
    }

    public DashboardSummaryDto getSummary() {
        long totalProjects = projectRepository.count();
        long totalTasks = taskRepository.count();
        long totalEpics = epicRepository.count();
        long totalStories = storyRepository.count();

        // Dynamic Task status counts
        Map<String, Long> taskStatusCount = statusRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Status::getName,
                        status -> taskRepository.countByStatusId(status.getId())
                ));

        // Epic status counts
        Map<String, Long> epicStatusCount = new HashMap<>();
        for (Epic.EpicStatus status : Epic.EpicStatus.values()) {
            epicStatusCount.put(status.name(), (long) epicRepository.findByStatus(status).size());
        }

        // Story status counts
        Map<String, Long> storyStatusCount = new HashMap<>();
        for (Story.StoryStatus status : Story.StoryStatus.values()) {
            storyStatusCount.put(status.name(), (long) storyRepository.findByStatus(status).size());
        }

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

        reminders.put("taskDueSoonCount", taskRepository.countByDueDateBetween(now, twoDaysLater));
        reminders.put("unassignedProjectCount", projectRepository.countByOwnerIdIsNull());
        reminders.put("sprintsEndingSoonCount", sprintRepository.countByEndDateBetween(now, twoDaysLater));
        reminders.put("todoStoryCount", storyRepository.countByStatus(Story.StoryStatus.TODO));

        return reminders;
    }

    public Map<String, Object> getDashboardData(Long userId) {
        // --- Task Status Counts for a specific user ---
        Map<String, Long> taskStatusMap = statusRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Status::getName,
                        status -> taskRepository.countByAssigneeIdAndStatusId(userId, status.getId())
                ));

        // --- Story Status Counts for a specific user ---
        Map<String, Long> storyStatusMap = new HashMap<>();
        for (Story.StoryStatus status : Story.StoryStatus.values()) {
            storyStatusMap.put(status.name(), storyRepository.countByAssigneeIdAndStatus(userId, status));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("taskStatus", taskStatusMap);
        result.put("storyStatus", storyStatusMap);

        return result;
    }
}
