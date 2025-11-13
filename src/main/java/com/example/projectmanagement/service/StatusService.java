package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.StatusDto;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Statuses;
import com.example.projectmanagement.repository.StatusesRepository;
import com.example.projectmanagement.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatusService {

    private final StatusesRepository repo;
    private final ProjectRepository projectRepo;

    @Transactional
    public StatusDto addCustomStatusDto(Long projectId, String name, boolean isBug, int sortOrder) {
        Project project = projectRepo.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project not found"));

        if (repo.existsByProjectIdAndNameIgnoreCase(projectId, name)) {
            throw new IllegalArgumentException("Status name already exists for this project.");
        }

        Statuses status = Statuses.builder()
                .name(name)
                .sortOrder(sortOrder)
                .isBug(isBug)
                .isPredefined(false)
                .isActive(true)
                .project(project)
                .build();

        Statuses saved = repo.save(status);
        return toDto(saved);
    }

    @Transactional
    public void reorderStatuses(Long projectId, List<Long> orderedIds) {
        List<Statuses> statuses = repo.findByProjectIdOrderBySortOrder(projectId);
        Map<Long, Statuses> map = new HashMap<>();
        statuses.forEach(s -> map.put(s.getStatusId(), s));

        int order = 1;
        for (Long id : orderedIds) {
            Statuses s = map.get(id);
            if (s != null) s.setSortOrder(order++);
        }
        repo.saveAll(statuses);
    }

    @Transactional
    public List<StatusDto> getActiveBugStatusesByProject(Long projectId) {
        List<Statuses> statuses = repo.findByProjectIdOrderBySortOrder(projectId);

        boolean isTestingActive = statuses.stream()
                .anyMatch(s -> "TESTING".equalsIgnoreCase(s.getName()) && Boolean.TRUE.equals(s.getIsActive()));

        if (!isTestingActive) {
            return List.of(); // No active bugs if TESTING is not active
        }

        return statuses.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()) && Boolean.TRUE.equals(s.getIsBug()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteStatus(Long statusId) {
        Statuses status = repo.findById(statusId)
                .orElseThrow(() -> new NoSuchElementException("Status not found"));
        if (Boolean.TRUE.equals(status.getIsPredefined())) {
            throw new IllegalStateException("Cannot delete a predefined status.");
        }
        repo.delete(status);
    }

    public List<StatusDto> getAllByProjectDto(Long projectId) {
        return repo.findByProjectIdOrderBySortOrder(projectId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    @Transactional
    public void createDefaultStatuses(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Project cannot be null");
        }

        List<Statuses> statuses = new ArrayList<>();

        // Normal statuses
        String[] normalStatuses = {"BACKLOG", "TODO", "IN_PROGRESS", "TESTING", "DONE"};
        for (int i = 0; i < normalStatuses.length; i++) {
            statuses.add(Statuses.builder()
                    .name(normalStatuses[i])
                    .sortOrder(i + 1)
                    .isBug(false)
                    .isPredefined(true)
                    .isActive(true)
                    .project(project)
                    .build()
            );
        }

        // Bug statuses
        String[] bugStatuses = {"OPEN", "IN_PROGRESS", "RESOLVED", "REOPEN", "CLOSED"};
        for (int i = 0; i < bugStatuses.length; i++) {
            statuses.add(Statuses.builder()
                    .name(bugStatuses[i])
                    .sortOrder(i + 1)
                    .isBug(true)
                    .isPredefined(true)
                    .isActive(true)
                    .project(project)
                    .build()
            );
        }

        repo.saveAll(statuses);
    }

    public List<StatusDto> getActiveStatusesByProject(Long projectId) {
        return repo.findByProjectIdOrderBySortOrder(projectId)
                .stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()) && Boolean.FALSE.equals(s.getIsBug()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<StatusDto> getActiveBugStatusesByProject(Long projectId) {
        // Fetch all statuses for the project
        List<Statuses> statuses = repo.findByProjectIdOrderBySortOrder(projectId);

        // Check if "TESTING" status is active
        boolean isTestingActive = statuses.stream()
                .anyMatch(s -> "TESTING".equalsIgnoreCase(s.getName()) && Boolean.TRUE.equals(s.getIsActive()));

        System.out.println(isTestingActive);

        if (!isTestingActive) {
            // If TESTING is not active, return empty list
            return List.of();
        }

        // Return only active bug statuses
        return statuses.stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()) && Boolean.TRUE.equals(s.getIsBug()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }




    private StatusDto toDto(Statuses status) {
        return StatusDto.builder()
                .statusId(status.getStatusId())
                .name(status.getName())
                .sortOrder(status.getSortOrder())
                .isBug(status.getIsBug())
                .isPredefined(status.getIsPredefined())
                .isActive(status.getIsActive())
                .projectId(status.getProject().getId())
                .build();
    }
}
