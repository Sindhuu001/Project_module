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
    public void toggleStatus(Long statusId, boolean active) {
        Statuses status = repo.findById(statusId)
                .orElseThrow(() -> new NoSuchElementException("Status not found"));
        status.setIsActive(active);

        if (!active && "TESTING".equalsIgnoreCase(status.getName())) {
            List<Statuses> all = repo.findByProjectIdOrderBySortOrder(status.getProject().getId());
            all.stream()
                    .filter(s -> Boolean.TRUE.equals(s.getIsBug()))
                    .forEach(s -> s.setIsActive(false));
            repo.saveAll(all);
        }

        repo.save(status);
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

        // Example default statuses
        List<Statuses> defaults = List.of(
                Statuses.builder()
                        .name("TO DO")
                        .sortOrder(1)
                        .isPredefined(true)
                        .isActive(true)
                        .isBug(false)
                        .project(project)
                        .build(),
                Statuses.builder()
                        .name("IN PROGRESS")
                        .sortOrder(2)
                        .isPredefined(true)
                        .isActive(true)
                        .isBug(false)
                        .project(project)
                        .build(),
                Statuses.builder()
                        .name("TESTING")
                        .sortOrder(3)
                        .isPredefined(true)
                        .isActive(true)
                        .isBug(false)
                        .project(project)
                        .build(),
                Statuses.builder()
                        .name("DONE")
                        .sortOrder(4)
                        .isPredefined(true)
                        .isActive(true)
                        .isBug(false)
                        .project(project)
                        .build()
        );

        repo.saveAll(defaults);
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
