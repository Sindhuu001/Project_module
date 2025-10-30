package com.example.projectmanagement.service;

//import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.dto.StoryDto;
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
public class StoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private EpicRepository epicRepository;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private ProjectRepository projectRepository;

    

    @Autowired
    private ModelMapper modelMapper;

    //private UserClient userClient;

    @Autowired
    private UserService userService;

    // ✅ Create Story
    public StoryDto createStory(StoryDto storyDto) {
        // Epic epic = epicRepository.findById(storyDto.getEpicId())
        //         .orElseThrow(() -> new RuntimeException("Epic not found with id: " + storyDto.getEpicId()));

        // Sprint sprint = sprintRepository.findById(storyDto.getSprintId())
        //         .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + storyDto.getSprintId()));

        Epic epic = null;
        if (storyDto.getEpicId() != null) {
            epic = epicRepository.findById(storyDto.getEpicId())
                    .orElseThrow(() -> new RuntimeException("Epic not found with id: " + storyDto.getEpicId()));
        }

        Sprint sprint = null;
        if (storyDto.getSprintId() != null) {
            sprint = sprintRepository.findById(storyDto.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + storyDto.getSprintId()));
        }
        Project project = projectRepository.findById(storyDto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + storyDto.getProjectId()));

        boolean exists = storyRepository.existsByTitleAndProjectIdAndEpicId(
                storyDto.getTitle(), storyDto.getProjectId(), storyDto.getEpicId()
        );
        if (exists) {
            throw new IllegalArgumentException("A story with the same title already exists under this epic and project.");
        }

        UserDto reporter = userService.getUserWithRoles(storyDto.getReporterId());
                

        Story story = modelMapper.map(storyDto, Story.class);
        story.setEpic(epic);
        story.setSprint(sprint);
        story.setProject(project);
        story.setReporterId(reporter.getId());

        if (storyDto.getAssigneeId() != null) {
            UserDto assignee = userService.getUserWithRoles(storyDto.getAssigneeId());
                
            story.setAssigneeId(assignee.getId());
        }

        Story savedStory = storyRepository.save(story);
        return convertToDto(savedStory);
    }

    // ✅ Get Story by ID
    @Transactional(readOnly = true)
    public StoryDto getStoryById(Long id) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + id));
        return convertToDto(story);
    }

    // ✅ Get All Stories
    @Transactional(readOnly = true)
    public List<StoryDto> getAllStories() {
        return storyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<StoryDto> getAllStories(Pageable pageable) {
        return storyRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    // ✅ Get Stories by Epic ID
    @Transactional(readOnly = true)
    public List<StoryDto> getStoriesByEpic(Long epicId) {
        return storyRepository.findByEpicId(epicId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ Get Stories by Project ID
    @Transactional(readOnly = true)
    public List<StoryDto> getStoriesByProjectId(Long projectId) {
        return storyRepository.findByProjectId(projectId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ Get Stories by Status
    @Transactional(readOnly = true)
    public List<StoryDto> getStoriesByStatus(Story.StoryStatus status) {
        return storyRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ Get Stories by Assignee
    @Transactional(readOnly = true)
    public List<StoryDto> getStoriesByAssignee(Long assigneeId) {
        return storyRepository.findByAssigneeId(assigneeId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ Get Stories by Sprint
    @Transactional(readOnly = true)
    public List<StoryDto> getStoriesBySprint(Long sprintId) {
        return storyRepository.findBySprintId(sprintId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ✅ Update Story
    public StoryDto updateStory(Long id, StoryDto storyDto) {
        Story existingStory = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + id));

        existingStory.setTitle(storyDto.getTitle());
        existingStory.setDescription(storyDto.getDescription());
        existingStory.setStatus(storyDto.getStatus());
        existingStory.setPriority(storyDto.getPriority());
        existingStory.setStoryPoints(storyDto.getStoryPoints());
        existingStory.setAcceptanceCriteria(storyDto.getAcceptanceCriteria());

        if (storyDto.getEpicId() != null &&
                (existingStory.getEpic() == null || !storyDto.getEpicId().equals(existingStory.getEpic().getId()))) {
            Epic newEpic = epicRepository.findById(storyDto.getEpicId())
                    .orElseThrow(() -> new RuntimeException("Epic not found with id: " + storyDto.getEpicId()));
            existingStory.setEpic(newEpic);
        }

        if (storyDto.getSprintId() != null &&
                (existingStory.getSprint() == null || !storyDto.getSprintId().equals(existingStory.getSprint().getId()))) {
            Sprint newSprint = sprintRepository.findById(storyDto.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + storyDto.getSprintId()));
            existingStory.setSprint(newSprint);
        }

        if (storyDto.getProjectId() != null &&
                (existingStory.getProject() == null || !storyDto.getProjectId().equals(existingStory.getProject().getId()))) {
            Project newProject = projectRepository.findById(storyDto.getProjectId())
                    .orElseThrow(() -> new RuntimeException("Project not found with id: " + storyDto.getProjectId()));
            existingStory.setProject(newProject);
        }

        if (storyDto.getAssigneeId() != null) {
            UserDto assignee = userService.getUserWithRoles(storyDto.getAssigneeId());
            existingStory.setAssigneeId(assignee.getId());
            
        } else {
            existingStory.setAssigneeId(null);
        }

        Story updatedStory = storyRepository.save(existingStory);
        return convertToDto(updatedStory);
    }

    // ✅ Delete Story
    public void deleteStory(Long id) {
        if (!storyRepository.existsById(id)) {
            throw new RuntimeException("Story not found with id: " + id);
        }
        storyRepository.deleteById(id);
    }

    // ✅ Search Stories with filters
    @Transactional(readOnly = true)
    public Page<StoryDto> searchStories(String title, Story.Priority priority, Long epicId, Long projectId, Long sprintId, Pageable pageable) {
        return storyRepository.searchByFilters(title, priority, epicId, projectId, sprintId, pageable)
                .map(this::convertToDto);
    }

    // ✅ Convert Entity to DTO (null-safe)
    StoryDto convertToDto(Story story) {
        StoryDto dto = modelMapper.map(story, StoryDto.class);

        dto.setEpicId(story.getEpic() != null ? story.getEpic().getId() : null);
        dto.setReporterId(story.getReporterId() != null ? story.getReporterId() : null);
        dto.setSprintId(story.getSprint() != null ? story.getSprint().getId() : null);
        dto.setProjectId(story.getProject() != null ? story.getProject().getId() : null);
        dto.setAssigneeId(story.getAssigneeId() != null ? story.getAssigneeId() : null);
        dto.setAssignee(story.getAssigneeId() != null ? userService.getUserWithRoles(story.getAssigneeId()) : null);
        dto.setReporter(story.getReporterId() != null ? userService.getUserWithRoles(story.getReporterId()) : null);

        return dto;
    }
    public List<StoryDto> getStoriesWithoutEpic(Long projectId) {
    return storyRepository.findByEpicIsNullAndProjectId(projectId)
            .stream()
            .map(story -> new StoryDto(story.getId(), story.getTitle(), story.getDescription()))
            .collect(Collectors.toList());
}
    public void assignStoryToSprint(Long storyId, Long sprintId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + storyId));

        if (sprintId != null) {
            Sprint sprint = sprintRepository.findById(sprintId)
                    .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + sprintId));
            story.setSprint(sprint);
            story.setProject(sprint.getProject()); // ✅ Add this line
        } else {
            story.setSprint(null);
            story.setProject(null); // ✅ Also clear project if unassigned
        }

        storyRepository.save(story);
    }

}
