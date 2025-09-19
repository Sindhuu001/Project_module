package com.example.projectmanagement.service;

import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.dto.ProjectDto;
import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.entity.Project;

import com.example.projectmanagement.exception.ValidationException;
import com.example.projectmanagement.repository.ProjectRepository;

import lombok.AllArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;
    
    // @Autowired
    // private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;
    
    @Autowired
    private UserClient userClient;

    @Autowired
    private UserService userService;

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

    // Validate owner via UMS
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
    project.setOwnerId(owner.getId()); // store UMS userId

    // Handle members
    List<Long> memberIds = projectDto.getMemberIds();
    if (memberIds != null && !memberIds.isEmpty()) {        
        // store member IDs in Project
        project.setMemberIds(memberIds);
    } else {
        project.setMemberIds(new ArrayList<>());
    }

    return convertToDto(projectRepository.save(project));
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
        return projectRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
    public List<ProjectDto> getProjectsByMember(Long userId) {
        return projectRepository.findByMemberId(userId).stream()
                .map(this::convertToDto)
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

        if (updatedDto.getOwnerId() != null && userClient.findExternalById(updatedDto.getOwnerId()) != null) {
            errors.add("Owner not found with id: " + updatedDto.getOwnerId());
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        // Apply changes
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
                UserDto owner = userService.getUserWithRoles(updatedDto.getOwnerId());
                existing.setOwnerId(owner.getId());
            }
        }

        // Update members if provided
        if (updatedDto.getMemberIds() != null) {
            List<Long> memberIds = updatedDto.getMemberIds();
            existing.setMemberIds(memberIds);
        }

        return convertToDto(projectRepository.save(existing));
    }

    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project not found with id: " + id);
        }
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
        ProjectDto dto = modelMapper.map(project, ProjectDto.class);

        dto.setOwnerId(project.getOwnerId());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setOwner(project.getOwnerId() != null ? userService.getUserWithRoles(project.getOwnerId()) : null);

        // Set memberIds and member UserDtos
        if (project.getMemberIds() != null) {
            List<Long> memberIds = project.getMemberIds();
            dto.setMemberIds(memberIds);
            
            dto.setMembers(userService.getUsersByIds(memberIds));
        }
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

}
