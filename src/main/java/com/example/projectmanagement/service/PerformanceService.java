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
    private EpicRepository epicRepository;

    @Autowired
    private StoryRepository storyRepository;

    private UserClient userClient;

    public List<EmployeePerformanceDto> getAllEmployeePerformance() {
        List<UserDto> users = userClient.findAllById();
        List<EmployeePerformanceDto> performanceList = new ArrayList<>();

        for (UserDto user : users) {
            List<Project> projects = projectRepository.findByMemberId(user.getId());
            List<Task> tasks = taskRepository.findByAssigneeId(user.getId());

            int totalTasks = tasks.size();
            int inProgress = (int) tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.IN_PROGRESS).count();
            int completed = (int) tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.DONE).count();
            int overdue = (int) tasks.stream().filter(t ->
                t.getDueDate() != null &&
                t.getDueDate().isBefore(LocalDateTime.now()) &&
                t.getStatus() != Task.TaskStatus.DONE
            ).count();

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
