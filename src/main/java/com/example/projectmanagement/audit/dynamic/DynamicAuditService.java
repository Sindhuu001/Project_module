package com.example.projectmanagement.audit.dynamic;

import com.example.projectmanagement.audit.base.AuditHistoryDto;
import com.example.projectmanagement.entity.Epic;
import com.example.projectmanagement.repository.EpicRepository;
import com.example.projectmanagement.repository.StoryRepository;
import com.example.projectmanagement.repository.TaskRepository;
import com.example.projectmanagement.repository.ProjectRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class DynamicAuditService {

    private final DynamicAuditRepository repository;

    private final EpicRepository epicRepository;
    private final StoryRepository storyRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public DynamicAuditService(
            DynamicAuditRepository repository,
            EpicRepository epicRepository,
            StoryRepository storyRepository,
            TaskRepository taskRepository,
            ProjectRepository projectRepository
    ) {
        this.repository = repository;
        this.epicRepository = epicRepository;
        this.storyRepository = storyRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public Map<String, Object> getRawRow(String tableName, Long id) {
        return repository.getRawRow(tableName, id);
    }

    public List<AuditHistoryDto> getHistoryById(String entityName, Long entityId) {

        String tableName = "audit_" + entityName.toLowerCase();

        List<Map<String, Object>> rows =
                repository.getHistoryRows(tableName, entityId);

        List<AuditHistoryDto> result = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            AuditHistoryDto dto = new AuditHistoryDto();
            dto.setTimestamp((LocalDateTime) row.get("timestamp"));
            dto.setOperation((String) row.get("operation"));
            dto.setOldData((String) row.get("old_data"));
            dto.setNewData((String) row.get("new_data"));
            result.add(dto);
        }

        return result;
    }

    // ===================================================
    // 1️⃣ FETCH OLD ENTITY BEFORE UPDATE
    // ===================================================
    public Object getEntityBeforeUpdate(String entityName, Long entityId) {

        return switch (entityName) {
            case "Epic" -> epicRepository.findEpicBasicById(entityId);
            case "Story" -> storyRepository.findStorybyStoryId(entityId);
            case "Task" -> taskRepository.findById(entityId).orElse(null);
            case "Project" -> projectRepository.findById(entityId).orElse(null);
            default -> null;
        };
    }

    // 2️⃣ FETCH ENTITY AFTER UPDATE
    public Object getEntityAfterUpdate(String entityName, Long entityId) {

        return switch (entityName) {
            case "Epic" -> epicRepository.findEpicBasicById(entityId);
            case "Story" -> storyRepository.findStorybyStoryId(entityId);
            case "Task" -> taskRepository.findById(entityId).orElse(null);
            case "Project" -> projectRepository.findById(entityId).orElse(null);
            default -> null;
        };
    }


    // ===================================================
    // 2️⃣ SAVE ENTITY AUDIT INTO DYNAMIC TABLE
    // ===================================================
    public void saveEntityAudit(String entity,
                                String entityId,
                                String oldData,
                                String newData,
                                String operation) {

        String tableName = "audit_" + entity.toLowerCase();

        repository.createTableIfNotExists(tableName);
        repository.insertAuditRow(tableName, entityId, oldData, newData, operation);
    }
}
