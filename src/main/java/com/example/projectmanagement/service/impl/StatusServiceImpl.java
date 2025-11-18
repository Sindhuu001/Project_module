package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.dto.StatusDto;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Status;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.StatusRepository;
import com.example.projectmanagement.repository.StoryRepository;
import com.example.projectmanagement.repository.TaskRepository;
import com.example.projectmanagement.service.StatusService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StatusServiceImpl implements StatusService {

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private StoryRepository storyRepository;

    @Override
    @Transactional
    public StatusDto addStatus(Long projectId, StatusDto statusDto) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Optional<Status> topStatus = statusRepository.findTopByProjectIdOrderBySortOrderDesc(projectId);
        int nextSortOrder = topStatus.map(s -> s.getSortOrder() + 1).orElse(1);

        Status status = modelMapper.map(statusDto, Status.class);
        status.setProject(project);
        status.setSortOrder(nextSortOrder);

        Status savedStatus = statusRepository.save(status);
        return modelMapper.map(savedStatus, StatusDto.class);
    }

    @Override
    public List<StatusDto> getStatusesByProject(Long projectId) {
        return statusRepository.findByProjectIdOrderBySortOrder(projectId).stream()
                .map(status -> modelMapper.map(status, StatusDto.class))
                .collect(Collectors.toList());
    }

    @Override
@Transactional
public void deleteStatus(Long statusId, Long newStatusId) {

    Status statusToDelete = statusRepository.findById(statusId)
            .orElseThrow(() -> new RuntimeException("Status not found"));

    Long projectId = statusToDelete.getProject().getId();

    // 1️⃣ Fetch tasks using this status
    List<Task> tasksWithStatus = taskRepository.findByStatusId(statusId);

    // 2️⃣ Fetch stories using this status
    List<Story> storiesWithStatus = storyRepository.findByStatusId(statusId);

    // 3️⃣ If tasks OR stories exist but newStatusId is missing → block deletion
    if (( !tasksWithStatus.isEmpty() || !storiesWithStatus.isEmpty() ) && newStatusId == null) {
        throw new RuntimeException(
                "Cannot delete status because it is used by tasks or stories. Provide a new status to move them."
        );
    }

    // 4️⃣ If newStatusId provided → move tasks + stories to new status
    if (newStatusId != null) {
        Status newStatus = statusRepository.findById(newStatusId)
                .orElseThrow(() -> new RuntimeException("New status not found"));

        // Move tasks
        tasksWithStatus.forEach(task -> task.setStatus(newStatus));
        taskRepository.saveAll(tasksWithStatus);

        // Move stories
        storiesWithStatus.forEach(story -> story.setStatus(newStatus));
        storyRepository.saveAll(storiesWithStatus);
    }

    // 5️⃣ Delete the status
    statusRepository.delete(statusToDelete);

    // 6️⃣ Reorder remaining statuses
    List<Status> remainingStatuses =
            statusRepository.findByProjectIdOrderBySortOrder(projectId);

    for (int i = 0; i < remainingStatuses.size(); i++) {
        remainingStatuses.get(i).setSortOrder(i + 1);
    }

    statusRepository.saveAll(remainingStatuses);
}


    @Override
    @Transactional
    public List<StatusDto> reorderStatuses(Map<Long, Integer> statusOrder) {
        List<Status> statusesToUpdate = statusRepository.findAllById(statusOrder.keySet());
        statusesToUpdate.forEach(status -> status.setSortOrder(statusOrder.get(status.getId())));
        List<Status> savedStatuses = statusRepository.saveAll(statusesToUpdate);
        return savedStatuses.stream()
                .map(status -> modelMapper.map(status, StatusDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StatusDto> syncStatuses(Long projectId, List<StatusDto> desiredStatuses) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Map<Long, Status> existingStatuses = statusRepository.findByProjectIdOrderBySortOrder(projectId).stream()
                .collect(Collectors.toMap(Status::getId, Function.identity()));

        List<Status> statusesToSave = desiredStatuses.stream().map(dto -> {
            Status status;
            if (dto.getId() != null) {
                status = existingStatuses.get(dto.getId());
                if (status == null) {
                    throw new RuntimeException("Status with id " + dto.getId() + " not found in project " + projectId);
                }
                existingStatuses.remove(dto.getId());
            } else {
                status = new Status();
                status.setProject(project);
            }
            status.setName(dto.getName());
            status.setSortOrder(dto.getSortOrder());
            return status;
        }).collect(Collectors.toList());

        if (!existingStatuses.isEmpty()) {
            statusRepository.deleteAll(existingStatuses.values());
        }

        List<Status> savedStatuses = statusRepository.saveAll(statusesToSave);
        return savedStatuses.stream()
                .map(status -> modelMapper.map(status, StatusDto.class))
                .collect(Collectors.toList());
    }
}
