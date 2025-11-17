package com.example.projectmanagement.service;

import com.example.projectmanagement.exception.ResourceNotFoundException;

import org.springframework.security.access.AccessDeniedException;

import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.dto.StoryDto;
import com.example.projectmanagement.dto.StoryCreateDto;
import com.example.projectmanagement.dto.StoryViewDto;
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
import java.util.Map;
import java.util.function.Function;
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
    private StatusRepository statusRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserClient userClient;

    @Transactional
    public StoryCreateDto createStory(StoryCreateDto dto) {

        Long projectId = dto.getProjectId();

        // Fetch Project (to get ownerId)
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Long ownerId = project.getOwnerId();

        // -----------------------------
        // VALIDATE REPORTER
        // -----------------------------
        if (dto.getReporterId() != null) {
            boolean isReporterValid = projectRepository.isUserPartOfProject(projectId, dto.getReporterId());
            if (!isReporterValid) {
                throw new RuntimeException("Reporter is not a member/owner of the project");
            }
        }

        // -----------------------------
        // VALIDATE ASSIGNEE
        // -----------------------------
        if (dto.getAssigneeId() != null) {
            boolean isAssigneeValid = projectRepository.isUserPartOfProject(projectId, dto.getAssigneeId());
            if (!isAssigneeValid) {
                throw new RuntimeException("Assignee is not a member/owner of the project");
            }
        }

        // -----------------------------
        // STORY CREATION
        // -----------------------------
        Story story = new Story();
        story.setTitle(dto.getTitle());
        story.setDescription(dto.getDescription());
        story.setAcceptanceCriteria(dto.getAcceptanceCriteria());
        story.setStoryPoints(dto.getStoryPoints());
        story.setAssigneeId(dto.getAssigneeId());
        story.setReporterId(dto.getReporterId());
        story.setPriority(dto.getPriority());

        story.setProject(
                projectRepository.findById(projectId)
                        .orElseThrow(() -> new RuntimeException("Project not found"))
        );

        if (dto.getEpicId() != null) {
            story.setEpic(
                    epicRepository.findById(dto.getEpicId())
                            .orElseThrow(() -> new RuntimeException("Epic not found"))
            );
        }

        if (dto.getSprintId() != null) {
            story.setSprint(
                    sprintRepository.findById(dto.getSprintId())
                            .orElseThrow(() -> new RuntimeException("Sprint not found"))
            );
        }

        story.setStatus(
                statusRepository.findById(dto.getStatusId())
                        .orElseThrow(() -> new RuntimeException("Status not found"))
        );

        Story saved = storyRepository.save(story);

        // -----------------------------
        // RETURN DTO
        // -----------------------------
        StoryCreateDto createdDto = new StoryCreateDto();
        createdDto.setTitle(saved.getTitle());
        createdDto.setDescription(saved.getDescription());
        createdDto.setAcceptanceCriteria(saved.getAcceptanceCriteria());
        createdDto.setStoryPoints(saved.getStoryPoints());
        createdDto.setAssigneeId(saved.getAssigneeId());
        createdDto.setReporterId(saved.getReporterId());
        createdDto.setProjectId(saved.getProject().getId());
        createdDto.setEpicId(saved.getEpic() != null ? saved.getEpic().getId() : null);
        createdDto.setSprintId(saved.getSprint() != null ? saved.getSprint().getId() : null);
        createdDto.setStatusId(saved.getStatus().getId());
        createdDto.setPriority(saved.getPriority());

        return createdDto;
    }

    @Transactional(readOnly = true)
    public Page<StoryViewDto> searchStoriesView(
            String title,
            Story.Priority priority,
            Long epicId,
            Long projectId,
            Long sprintId,
            Pageable pageable
    ) {
        return storyRepository.searchByFilters(title, priority, epicId, projectId, sprintId, pageable)
                .map(this::convertToViewDto);
    }

    @Transactional(readOnly = true)
    public StoryViewDto getStoryViewById(Long id) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + id));
        return convertToViewDto(story);
    }


    @Transactional(readOnly = true)
    public StoryDto getStoryById(Long id) {
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + id));
        return convertToDto(story);
    }

    @Transactional(readOnly = true)
    public List<StoryDto> getAllStories() {
        return storyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<StoryDto> getAllStories(Pageable pageable) {
        Map<Long, UserDto> userMap = userClient.findAll().stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return storyRepository.findAll(pageable)
                .map(story -> convertToDto1(story, userMap));
    }

    @Transactional(readOnly = true)
    public List<StoryViewDto> getStoriesByEpic(Long epicId) {
        return storyRepository.findByEpicId(epicId).stream()
                .map(this::convertToViewDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StoryDto> getStoriesByProjectId(Long projectId) {
        Map<Long, UserDto> userMap = userClient.findAll().stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return storyRepository.findByProjectId(projectId).stream()
                .map(story -> convertToDto1(story, userMap))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StoryViewDto> getStoriesByStatus(Long statusId) {
        return storyRepository.findByStatusId(statusId).stream()
                .map(this::convertToViewDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StoryDto> getStoriesByAssignee(Long assigneeId) {
        return storyRepository.findByAssigneeId(assigneeId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StoryViewDto> getStoriesBySprint(Long sprintId) {
        return storyRepository.findBySprintId(sprintId).stream()
                .map(this::convertToViewDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public StoryCreateDto updateStory(Long id, StoryCreateDto dto) {

        // Fetch existing story
        Story story = storyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found with id: " + id));

        Long projectId = story.getProject().getId();

        // -----------------------------------------
        // CHECK: Reporter must be member/owner
        // -----------------------------------------
        if (!projectRepository.isUserPartOfProject(projectId, dto.getReporterId())) {
            throw new AccessDeniedException("Reporter is not a member/owner of this project");
        }

        // -----------------------------------------
        // CHECK: Assignee (optional)
        // -----------------------------------------
        if (dto.getAssigneeId() != null) {
            boolean isAssigneeValid = projectRepository.isUserPartOfProject(projectId, dto.getAssigneeId());

            if (!isAssigneeValid) {
                throw new AccessDeniedException("Assignee is not a member/owner of this project");
            }

            story.setAssigneeId(dto.getAssigneeId());
        } else {
            story.setAssigneeId(null);
        }

        // -----------------------------------------
        // UPDATE BASIC FIELDS
        // -----------------------------------------
        story.setTitle(dto.getTitle());
        story.setDescription(dto.getDescription());
        story.setAcceptanceCriteria(dto.getAcceptanceCriteria());
        story.setStoryPoints(dto.getStoryPoints());
        story.setReporterId(dto.getReporterId());
        story.setPriority(dto.getPriority());

        // -----------------------------------------
        // STATUS UPDATE
        // -----------------------------------------
        Status status = statusRepository.findById(dto.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Status not found"));
        story.setStatus(status);

        // -----------------------------------------
        // SPRINT UPDATE (nullable)
        // -----------------------------------------
        if (dto.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(dto.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
            story.setSprint(sprint);
        } else {
            story.setSprint(null); // move to backlog
        }

        // -----------------------------------------
        // EPIC UPDATE (nullable)
        // -----------------------------------------
        if (dto.getEpicId() != null) {
            Epic epic = epicRepository.findById(dto.getEpicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));
            story.setEpic(epic);
        } else {
            story.setEpic(null);
        }

        Story saved = storyRepository.save(story);

        // -----------------------------------------
        // RETURN DTO
        // -----------------------------------------
        StoryCreateDto updated = new StoryCreateDto();
        updated.setTitle(saved.getTitle());
        updated.setDescription(saved.getDescription());
        updated.setAcceptanceCriteria(saved.getAcceptanceCriteria());
        updated.setStoryPoints(saved.getStoryPoints());
        updated.setAssigneeId(saved.getAssigneeId());
        updated.setReporterId(saved.getReporterId());
        updated.setProjectId(projectId);
        updated.setEpicId(saved.getEpic() != null ? saved.getEpic().getId() : null);
        updated.setSprintId(saved.getSprint() != null ? saved.getSprint().getId() : null);
        updated.setStatusId(saved.getStatus().getId());
        updated.setPriority(saved.getPriority());

        return updated;
    }



    public StoryDto updateStoryStatus(Long storyId, Long statusId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found with id: " + storyId));
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Status not found with id: " + statusId));
        story.setStatus(status);
        Story updatedStory = storyRepository.save(story);
        return convertToDto(updatedStory);
    }

    public void deleteStory(Long id) {
        if (!storyRepository.existsById(id)) {
            throw new RuntimeException("Story not found with id: " + id);
        }
        storyRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<StoryDto> searchStories(String title, Story.Priority priority, Long epicId, Long projectId, Long sprintId, Pageable pageable) {
        List<UserDto> allUsers = userClient.findAll();
        Map<Long, UserDto> userMap = allUsers.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return storyRepository.searchByFilters(title, priority, epicId, projectId, sprintId, pageable)
                .map(story -> convertToDto1(story, userMap)
                );
    }

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
        return storyRepository.findByEpicIsNullAndProjectIdAndSprintIdIsNull(projectId)
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
            story.setProject(sprint.getProject());
        } else {
            story.setSprint(null);
            story.setProject(null);
        }

        storyRepository.save(story);
    }

    public StoryDto convertToDto1(Story story, Map<Long, UserDto> userMap) {
        StoryDto dto = modelMapper.map(story, StoryDto.class);
        dto.setEpicId(story.getEpic() != null ? story.getEpic().getId() : null);
        dto.setReporterId(story.getReporterId() != null ? story.getReporterId() : null);
        dto.setSprintId(story.getSprint() != null ? story.getSprint().getId() : null);
        dto.setProjectId(story.getProject() != null ? story.getProject().getId() : null);
        dto.setAssigneeId(story.getAssigneeId() != null ? story.getAssigneeId() : null);
        dto.setAssignee(story.getAssigneeId() != null ? userMap.get(story.getAssigneeId()) : null);
        dto.setReporter(story.getReporterId() != null ? userMap.get(story.getReporterId()) : null);
        return dto;
    }

    public StoryViewDto convertToViewDto(Story story) {
        StoryViewDto dto = new StoryViewDto();

        dto.setId(story.getId());
        dto.setTitle(story.getTitle());
        dto.setDescription(story.getDescription());
        dto.setAcceptanceCriteria(story.getAcceptanceCriteria());
        dto.setStoryPoints(story.getStoryPoints());
        dto.setPriority(story.getPriority().name());

        // Status
        if (story.getStatus() != null) {
            dto.setStatusId(story.getStatus().getId());
            dto.setStatusName(story.getStatus().getName());
        }

        // Epic
        if (story.getEpic() != null) {
            dto.setEpicId(story.getEpic().getId());
            dto.setEpicTitle(story.getEpic().getName());
        }

        // Project
        if (story.getProject() != null) {
            dto.setProjectId(story.getProject().getId());
            dto.setProjectName(story.getProject().getName());
        }

        // Sprint
        if (story.getSprint() != null) {
            dto.setSprintId(story.getSprint().getId());
            dto.setSprintName(story.getSprint().getName());
        }

        // Assignee
        if (story.getAssigneeId() != null) {
            UserDto assignee = userService.getUserWithRoles(story.getAssigneeId());
            dto.setAssigneeId(assignee.getId());
            dto.setAssigneeName(assignee.getName());
        }

        // Reporter
        if (story.getReporterId() != null) {
            UserDto reporter = userService.getUserWithRoles(story.getReporterId());
            dto.setReporterId(reporter.getId());
            dto.setReporterName(reporter.getName());
        }

        // Tasks inside story
        dto.setTaskIds(
                story.getTasks().stream()
                        .map(Task::getId)
                        .toList()
        );

        dto.setCreatedAt(story.getCreatedAt());
        dto.setUpdatedAt(story.getUpdatedAt());

        return dto;
    }



}
