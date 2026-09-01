package com.example.projectmanagement.service;

import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.dto.EmployeePerformanceDto;
import com.example.projectmanagement.dto.EpicWithStoriesDto;
import com.example.projectmanagement.dto.StoryWithTasksDto;
import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserClient userClient;

    // No need to inject StatusRepository as we will check by name

    @Transactional(readOnly = true)
    public List<EmployeePerformanceDto> getAllEmployeePerformance() {
        List<UserDto> users = userClient.findAll();
        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> userIds = users.stream().map(UserDto::getId).collect(Collectors.toList());

        // Single batched query for all tasks (with story + epic pre-fetched) instead of
        // one findByAssigneeId query per user, avoiding N+1 lookups when traversing
        // task -> story -> epic below.
        Map<Long, List<Task>> tasksByAssignee = taskRepository.findByAssigneeIdInWithStoryAndEpic(userIds).stream()
                .collect(Collectors.groupingBy(Task::getAssigneeId));

        // Single query for all projects with members pre-fetched, instead of one
        // findByMemberId query per user.
        Map<Long, List<Project>> projectsByMember = new HashMap<>();
        for (Project project : projectRepository.findAllWithMembers()) {
            for (Long memberId : project.getMemberIds()) {
                projectsByMember.computeIfAbsent(memberId, k -> new ArrayList<>()).add(project);
            }
        }

        List<EmployeePerformanceDto> performanceList = new ArrayList<>(users.size());

        for (UserDto user : users) {
            List<Project> projects = projectsByMember.getOrDefault(user.getId(), Collections.emptyList());
            List<Task> tasks = tasksByAssignee.getOrDefault(user.getId(), Collections.emptyList());

            int totalTasks = tasks.size();

            // Updated logic using dynamic statuses by name
            int completed = (int) tasks.stream()
                    .filter(t -> t.getStatus() != null && "Done".equalsIgnoreCase(t.getStatus().getName()))
                    .count();

            int inProgress = (int) tasks.stream()
                    .filter(t -> {
                        if (t.getStatus() == null) return false;
                        String statusName = t.getStatus().getName();
                        return !"Done".equalsIgnoreCase(statusName) &&
                               !"To Do".equalsIgnoreCase(statusName) &&
                               !"Backlog".equalsIgnoreCase(statusName);
                    })
                    .count();

            int overdue = (int) tasks.stream()
                    .filter(t -> t.getDueDate() != null &&
                                 t.getDueDate().isBefore(LocalDateTime.now()) &&
                                 (t.getStatus() != null && !"Done".equalsIgnoreCase(t.getStatus().getName())))
                    .count();

            // Grouping tasks -> stories -> epics
            Map<Epic, Map<Story, List<Task>>> epicStoryTaskMap = new HashMap<>();

            for (Task task : tasks) {
                Story story = task.getStory();
                if (story == null) continue;

                Epic epic = story.getEpic();
                if (epic == null) continue;

                epicStoryTaskMap
                    .computeIfAbsent(epic, k -> new HashMap<>())
                    .computeIfAbsent(story, k -> new ArrayList<>())
                    .add(task);
            }

            List<EpicWithStoriesDto> epicDtos = new ArrayList<>();
            for (Map.Entry<Epic, Map<Story, List<Task>>> epicEntry : epicStoryTaskMap.entrySet()) {
                Epic epic = epicEntry.getKey();
                EpicWithStoriesDto epicDto = new EpicWithStoriesDto();
                epicDto.setEpicId(epic.getId());
                epicDto.setEpicName(epic.getName());

                List<StoryWithTasksDto> storyDtos = new ArrayList<>();
                for (Map.Entry<Story, List<Task>> storyEntry : epicEntry.getValue().entrySet()) {
                    Story story = storyEntry.getKey();
                    StoryWithTasksDto storyDto = new StoryWithTasksDto();
                    storyDto.setStoryId(story.getId());
                    storyDto.setStoryTitle(story.getTitle());
                    storyDto.setTaskTitles(
                        storyEntry.getValue().stream()
                                  .map(Task::getTitle)
                                  .collect(Collectors.toList())
                    );
                    storyDtos.add(storyDto);
                }

                epicDto.setStories(storyDtos);
                epicDtos.add(epicDto);
            }

            EmployeePerformanceDto dto = new EmployeePerformanceDto();
            dto.setEmployeeName(user.getName());
            dto.setEmployeeEmail(user.getEmail());
            dto.setProjectNames(projects.stream().map(Project::getName).collect(Collectors.toList()));
            dto.setTotalTasks(totalTasks);
            dto.setTasksInProgress(inProgress);
            dto.setTasksCompleted(completed);
            dto.setTasksOverdue(overdue);
            dto.setEpics(epicDtos);

            performanceList.add(dto);
        }

        return performanceList;
    }
}
