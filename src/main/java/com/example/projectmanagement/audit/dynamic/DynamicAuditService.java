package com.example.projectmanagement.audit.dynamic;

import com.example.projectmanagement.audit.base.AuditHistoryDto;
import com.example.projectmanagement.audit.base.AuditTrail.AuditEntityType;
import com.example.projectmanagement.entity.Epic;
import com.example.projectmanagement.entity.Status;
import com.example.projectmanagement.entity.Story;
import com.example.projectmanagement.repository.EpicRepository;
import com.example.projectmanagement.repository.StoryRepository;
import com.example.projectmanagement.repository.TaskRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.StatusRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class DynamicAuditService {

    private final DynamicAuditRepository repository;

    private final EpicRepository epicRepository;
    private final StoryRepository storyRepository;
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final StatusRepository statusRepository;

    public DynamicAuditService(
            DynamicAuditRepository repository,
            EpicRepository epicRepository,
            StoryRepository storyRepository,
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            StatusRepository statusRepository
    ) {
        this.repository = repository;
        this.epicRepository = epicRepository;
        this.storyRepository = storyRepository;
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.statusRepository= statusRepository;
    }

    public Map<String, Object> getRawRow(String tableName, Long id) {
        return repository.getRawRow(tableName, id);
    }

    public List<AuditHistoryDto> getHistoryById(AuditEntityType entityName, Long entityId) {

        String tableName = "audit_" + entityName.name().toLowerCase();
        List<Map<String, Object>> rows = repository.getHistoryRows(tableName, entityId);

        List<AuditHistoryDto> result = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        String userName=getUserNameFromJwt();

        for (Map<String, Object> row : rows) {

            String operation = (String) row.get("operation");
            String oldJson = (String) row.get("old_data");
            String newJson = (String) row.get("new_data");

            // Parse JSON safely
            Map<String, Object> oldMap = parseJson(oldJson, mapper);
            Map<String, Object> newMap = parseJson(newJson, mapper);

            // ============================
            // CASE 1: CREATE / DELETE
            // ============================
            if (operation.equals("CREATE") || operation.equals("DELETE")) {

                AuditHistoryDto dto = new AuditHistoryDto();
                dto.setUserName(userName);
                dto.setTimestamp((LocalDateTime) row.get("timestamp"));
                dto.setAction(operation.equals("CREATE") ? "created" : "deleted");

                // Raw JSON
                // dto.setOldData(oldJson);
                // dto.setNewData(newJson);
                dto.setOperation(operation);

                result.add(dto);
                continue;
            }

            // ============================
            // CASE 2: UPDATE -> Field-wise
            // ============================
            for (String key : newMap.keySet()) {

                if ("updated_at".equals(key)) {
                    continue;
                }

                Object oldVal = oldMap.get(key);
                Object newVal = newMap.get(key);

                if (!Objects.equals(oldVal, newVal)) {

                    AuditHistoryDto dto = new AuditHistoryDto();
                    dto.setUserName(userName);

                    dto.setTimestamp((LocalDateTime) row.get("timestamp"));
                    dto.setAction("changed " + key);
                    dto.setField(key);
                    dto.setOldValue(formatValue(key, oldVal));
                    dto.setNewValue(formatValue(key, newVal));

                    // raw JSON
                    dto.setOperation(operation);
                    // dto.setOldData(oldJson);
                    // dto.setNewData(newJson);

                    result.add(dto);
                }
            }
        }

        return result;
    }

    // Safe JSON conversion
    private Map<String, Object> parseJson(String json, ObjectMapper mapper) {
        try {
            return mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            return new HashMap<>(); // fallback
        }
    }

    // Formatting values for display
    private String formatValue(String field, Object val) {
        if (val == null) return "None";

        try {
            switch (field) {

                case "status_id":
                    Status status = statusRepository.findStatusById(Long.valueOf(val.toString()));
                    return status != null ? status.getName():"unknown Status";

                case "story_id":    // 11 → "User login story"
                    Story story = storyRepository.findStorybyStoryId(Long.valueOf(val.toString()));
                    return story != null ? story.getTitle(): "unknown Story";

                case "epic_id":     // 3 → "Authentication Module"
                    Epic epic = epicRepository.findEpicBasicById(Long.valueOf(val.toString()));
                    return epic != null ? epic.getName() : "Unknown Epic";

                case "task_id":     // 4 → "Fix UI bug"
                    return taskRepository.findById(Long.valueOf(val.toString()))
                            .map(t -> t.getTitle())
                            .orElse("Unknown Task");

                case "project_id":
                    return projectRepository.findById(Long.valueOf(val.toString()))
                            .map(p -> p.getName())
                            .orElse("Unknown Project");

                case "priority":
                    return val.toString();

                default:
                    return val.toString();
            }
        } catch (Exception e) {
            return val.toString(); // fallback
        }
    }


    private String getUserNameFromJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth instanceof JwtAuthenticationToken token) {
            Jwt jwt = token.getToken();

            if (jwt.hasClaim("name")) {
                System.out.println("Username from JWT: " + jwt.getClaimAsString("name"));
                return jwt.getClaimAsString("name");
            }
        }
        return "Unknown User";
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
    public void saveEntityAudit(
        String entity,
        String entityId,
        String ipAddress,
        LocalDateTime timestamp,
        String operation,
        String oldData,
        String newData,
        String host,
        Long userId,
        String endpoint
    ) {
        String tableName = "audit_" + entity.toLowerCase();

        repository.createTableIfNotExists(tableName);

        repository.insertAuditRow(
                tableName,
                entityId,
                operation,
                userId,
                oldData,
                newData,
                ipAddress,
                host,
                timestamp,
                endpoint
        );
    }

    

}
