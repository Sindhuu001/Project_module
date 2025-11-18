package com.example.projectmanagement.service;

import com.example.projectmanagement.ExternalDTO.ProjectIdName;
import com.example.projectmanagement.ExternalDTO.ProjectTasksDto;
import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.config.ProjectStatusProperties;
import com.example.projectmanagement.dto.ProjectDto;
import com.example.projectmanagement.dto.ProjectSummary;
import com.example.projectmanagement.dto.StatusDto;
import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.exception.ValidationException;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.StatusRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserClient userClient;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectStatusProperties projectStatusProperties;

    @Autowired
    private StatusService statusService;

    @Autowired
    private StatusRepository statusRepository;

    public ProjectDto createProject(ProjectDto projectDto) {
        List<String> errors = new ArrayList<>();

        if (projectDto.getName() == null || projectDto.getName().trim().isEmpty()) {
            errors.add("Project name must not be empty.");
        }

        if (projectDto.getProjectKey() == null || projectDto.getProjectKey().trim().isEmpty()) {
            errors.add("Project key must be provided.");
        } else if (projectRepository.existsByProjectKey(projectDto.getProjectKey())) {
            errors.add("Project with key " + projectDto.getProjectKey() + " already exists.");
        }

        if (projectDto.getStartDate() == null) {
            errors.add("Start date is required.");
        }

        if (projectDto.getStartDate() != null && projectDto.getEndDate() != null &&
                projectDto.getStartDate().isAfter(projectDto.getEndDate())) {
            errors.add("Start date cannot be after end date.");
        }

        UserDto owner;
        try {
            owner = userService.getUserWithRoles(projectDto.getOwnerId());
        } catch (Exception e) {
            errors.add("Valid owner ID is required.");
            owner = null;
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        Project project = modelMapper.map(projectDto, Project.class);
        project.setOwnerId(owner.getId());

        if (projectDto.getCurrentStage() != null) {
            project.setCurrentStage(projectDto.getCurrentStage());
        } else {
            project.setCurrentStage(Project.ProjectStage.INITIATION);
        }

        Set<Long> memberIds = projectDto.getMemberIds();
        if (memberIds != null && !memberIds.isEmpty()) {
            project.setMemberIds(memberIds);
        } else {
            project.setMemberIds(new HashSet<>());
        }

        Project savedProject = projectRepository.save(project);

        projectStatusProperties.getDefaultStatuses().forEach(statusProperty -> {
            StatusDto defaultStatusDto = new StatusDto();
            defaultStatusDto.setName(statusProperty.getName());
            // The sortOrder will be calculated by the addStatus method
            statusService.addStatus(savedProject.getId(), defaultStatusDto);
        });

        return convertToDto(savedProject);
    }

    @Transactional(readOnly = true)
    public Long getProjectCount() {
        return projectRepository.countByStatus(Project.ProjectStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        return convertToDto(project);
    }

    @Transactional(readOnly = true)
    public ProjectDto getProjectByKey(String projectKey) {
        Project project = projectRepository.findByProjectKey(projectKey)
                .orElseThrow(() -> new RuntimeException("Project not found with key: " + projectKey));
        return convertToDto(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjects() {
        long start = System.currentTimeMillis();
        List<UserDto> allUsers = userClient.findAll();
        Map<Long, UserDto> userMap = allUsers.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        List<ProjectDto> dtos = projectRepository.findAll().stream()
                .map(project -> convertToDto1(project, userMap))
                .collect(Collectors.toList());
        System.out.println("*****************Time taken to fetch all projects with users: " + (System.currentTimeMillis() - start) + " ms");
        return dtos;
    }

    public ProjectDto convertToDto1(Project project, Map<Long, UserDto> userMap) {
        long start = System.currentTimeMillis();
        ProjectDto dto = modelMapper.map(project, ProjectDto.class);
        dto.setOwner(userMap.get(project.getOwnerId()));
        dto.setMembers(
                project.getMemberIds().stream()
                        .map(userMap::get)
                        .collect(Collectors.toList())
        );
        System.out.println("###########################Time taken to convert single Project to ProjectDto: " + (System.currentTimeMillis() - start) + " ms");
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<ProjectDto> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsByOwner(Long ownerId) {
        return projectRepository.findByOwnerId(ownerId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getActiveProjectsByOwner1(Long ownerId) {
        return projectRepository.findByOwnerId(ownerId).stream()
                .filter(project -> project.getStatus() == Project.ProjectStatus.ACTIVE)
                .map(project -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", project.getId());
                    map.put("name", project.getName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsByMember(Long userId) {
        List<UserDto> allUsers = userClient.findAll();
        Map<Long, UserDto> userMap = allUsers.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return projectRepository.findByMemberId(userId).stream()
                .map(project -> convertToDto1(project, userMap))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> getProjectsByStatus(Project.ProjectStatus status) {
        return projectRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public ProjectDto updateProject(Long id, ProjectDto updatedDto) {
        List<String> errors = new ArrayList<>();
        Project existing = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));

        Project.ProjectStatus existingStatus = existing.getStatus();
        Project.ProjectStatus newStatus = updatedDto.getStatus();

        if (existingStatus == Project.ProjectStatus.ARCHIVED && newStatus != Project.ProjectStatus.ACTIVE) {
            errors.add("Cannot update an archived project unless status is changed to ACTIVE.");
        }

        if (updatedDto.getStartDate() != null && updatedDto.getEndDate() != null &&
                updatedDto.getStartDate().isAfter(updatedDto.getEndDate())) {
            errors.add("Start date cannot be after end date.");
        }

        if (updatedDto.getOwnerId() != null) {
            try {
                Object ownerResponse = userClient.findExternalById(updatedDto.getOwnerId());
                if (ownerResponse == null || (ownerResponse instanceof List && ((List<?>) ownerResponse).isEmpty())) {
                    errors.add("Owner not found with id: " + updatedDto.getOwnerId());
                }
            } catch (Exception e) {
                errors.add("Owner not found with id: " + updatedDto.getOwnerId());
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        if (existingStatus == Project.ProjectStatus.ARCHIVED && newStatus == Project.ProjectStatus.ACTIVE) {
            existing.setStatus(Project.ProjectStatus.ACTIVE);
        } else {
            existing.setName(updatedDto.getName());
            existing.setDescription(updatedDto.getDescription());
            existing.setProjectKey(updatedDto.getProjectKey());
            existing.setStartDate(updatedDto.getStartDate());
            existing.setEndDate(updatedDto.getEndDate());
            existing.setStatus(updatedDto.getStatus());

            if (updatedDto.getOwnerId() != null) {
                existing.setOwnerId(updatedDto.getOwnerId());
            }

            if (updatedDto.getCurrentStage() != null) {
                existing.setCurrentStage(updatedDto.getCurrentStage());
            }
        }

        if (updatedDto.getMemberIds() != null) {
            existing.setMemberIds(new HashSet<>(updatedDto.getMemberIds()));
        }

        Project saved = projectRepository.save(existing);
        return convertToDto(saved);
    }

    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project not found with id: " + id);
        }
        statusRepository.deleteByProjectId(id);
        projectRepository.deleteById(id);
    }

    public ProjectDto addMemberToProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        if (!project.getMemberIds().contains(userId)) {
            project.getMemberIds().add(userId);
            projectRepository.save(project);
        }

        return convertToDto(project);
    }

    public ProjectDto removeMemberFromProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        project.getMemberIds().remove(userId);
        projectRepository.save(project);

        return convertToDto(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDto> searchProjects(String name, Project.ProjectStatus status, Pageable pageable) {
        if (name != null && status != null) {
            return projectRepository.findByNameContainingAndStatus(name, status, pageable)
                    .map(this::convertToDto);
        } else if (name != null) {
            return projectRepository.findByNameContaining(name, pageable)
                    .map(this::convertToDto);
        } else if (status != null) {
            return projectRepository.findByStatus(status, pageable)
                    .map(this::convertToDto);
        } else {
            return projectRepository.findAll(pageable)
                    .map(this::convertToDto);
        }
    }

    public ProjectDto convertToDto(Project project) {
        long start = System.currentTimeMillis();
        ProjectDto dto = ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .projectKey(project.getProjectKey())
                .description(project.getDescription())
                .status(project.getStatus())
                .currentStage(project.getCurrentStage())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();

        dto.setOwnerId(project.getOwnerId());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setOwner(project.getOwnerId() != null ? userService.getUserWithRoles(project.getOwnerId()) : null);

        if (project.getMemberIds() != null) {
            Set<Long> memberIds = project.getMemberIds();
            dto.setMemberIds(memberIds);
            dto.setMembers(userService.getUsersByIds(new ArrayList<>(memberIds)));
        }

        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&Time taken to convert Project to ProjectDto: " + (System.currentTimeMillis() - start) + " ms");
        return dto;
    }

    public ProjectDto unarchiveProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (project.getStatus() != Project.ProjectStatus.ARCHIVED) {
            throw new RuntimeException("Only archived projects can be unarchived.");
        }

        project.setStatus(Project.ProjectStatus.ACTIVE);
        Project updated = projectRepository.save(project);
        return convertToDto(updated);
    }

    public List<ProjectTasksDto> getAllProjectsWithTasks() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream().map(project -> {
            List<ProjectTasksDto.TaskDto> taskDtos = project.getTasks().stream()
                    .map(task -> new ProjectTasksDto.TaskDto(task.getId(), task.getTitle()))
                    .collect(Collectors.toList());
            return new ProjectTasksDto(project.getId(), project.getName(), taskDtos);
        }).collect(Collectors.toList());
    }

    public List<ProjectIdName> getAllProjectInfo() {
        return projectRepository.findAll().stream()
                .filter(p -> p.getStatus().equals(Project.ProjectStatus.ACTIVE))
                .map(p -> {
                    ProjectIdName pro = new ProjectIdName();
                    pro.setId(p.getId());
                    pro.setName(p.getName());
                    return pro;
                })
                .collect(Collectors.toList());
    }

    public List<UserDto> getProjectMembers(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        return userService.getUsersByIds(new ArrayList<>(project.getMemberIds()));
    }

    public List<UserDto> getProjectMembersOwner(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
        return userService.getUsersByIds(new ArrayList<>(project.getMemberIds()));
    }

    public List<ProjectIdName> getActiveProjectsByMember(Long userId) {
        return projectRepository.findByMemberIdsAndStatus(userId, Project.ProjectStatus.ACTIVE).stream()
                .map(project -> {
                    ProjectIdName pro = new ProjectIdName();
                    pro.setId(project.getId());
                    pro.setName(project.getName());
                    return pro;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getProjectOwner(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        Long ownerId = project.getOwnerId();
        if (ownerId == null) {
            throw new RuntimeException("Project has no assigned owner.");
        }
        return userService.getUserWithRoles(ownerId);
    }

    public List<ProjectSummary> getProjectSummariesByOwner(Long ownerId) {
        return projectRepository.findProjectSummariesByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public List<ProjectSummary> getAccessibleProjects(Long userId) {
        return projectRepository.findAccessibleProjectSummaries(userId);
    }

}
