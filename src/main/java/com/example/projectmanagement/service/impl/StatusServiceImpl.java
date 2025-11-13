package com.example.projectmanagement.service.impl;

import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Status;
import com.example.projectmanagement.entity.Task;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.StatusRepository;
import com.example.projectmanagement.repository.TaskRepository;
import com.example.projectmanagement.service.StatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class StatusServiceImpl implements StatusService {

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Override
    @Transactional
    public Status addStatus(Long projectId, Status status) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        status.setProject(project);
        return statusRepository.save(status);
    }

    @Override
    public List<Status> getStatusesByProject(Long projectId) {
        return statusRepository.findByProjectIdOrderBySortOrder(projectId);
    }

    @Override
    @Transactional
    public void deleteStatus(Long statusId, Long newStatusId) {
        Status statusToDelete = statusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Status not found"));

        List<Task> tasksWithStatus = taskRepository.findByStatusId(statusId);

        if (!tasksWithStatus.isEmpty()) {
            if (newStatusId == null) {
                throw new RuntimeException("Cannot delete status with tasks without providing a new status");
            }
            Status newStatus = statusRepository.findById(newStatusId)
                    .orElseThrow(() -> new RuntimeException("New status not found"));
            tasksWithStatus.forEach(task -> task.setStatus(newStatus));
            taskRepository.saveAll(tasksWithStatus);
        }

        statusRepository.delete(statusToDelete);
    }

    @Override
    @Transactional
    public List<Status> reorderStatuses(Map<Long, Integer> statusOrder) {
        List<Status> statusesToUpdate = statusRepository.findAllById(statusOrder.keySet());
        statusesToUpdate.forEach(status -> status.setSortOrder(statusOrder.get(status.getId())));
        return statusRepository.saveAll(statusesToUpdate);
    }
}
