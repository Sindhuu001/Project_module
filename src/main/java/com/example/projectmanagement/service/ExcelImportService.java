package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.ExcelImportResultDto;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private final ProjectRepository projectRepository;
    private final EpicRepository epicRepository;
    private final StoryRepository storyRepository;
    private final TaskRepository taskRepository;
    private final StatusRepository statusRepository;
    private final ProjectExcelImportRepository excelImportRepository;

    @Transactional
    public ExcelImportResultDto importFromExcel(Long projectId, MultipartFile file, Authentication authentication) throws Exception {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));

        List<Status> statuses = statusRepository.findByProjectIdOrderBySortOrder(projectId);
        if (statuses.isEmpty()) {
            throw new RuntimeException("No statuses configured for project " + projectId);
        }

        // Build case-insensitive status lookup map for this project
        Map<String, Status> statusMap = statuses.stream()
                .collect(Collectors.toMap(
                        s -> s.getName().toLowerCase().trim(),
                        Function.identity(),
                        (a, b) -> a
                ));
        Status defaultStatus = statuses.get(0);

        Long userId = extractUserId(authentication);

        List<String> errors = new ArrayList<>();
        int epicsCreated = 0;
        int storiesCreated = 0;
        int tasksCreated = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            if (workbook.getNumberOfSheets() > 1) {
                throw new RuntimeException("Excel file contains " + workbook.getNumberOfSheets()
                        + " sheets. Only a single sheet is allowed.");
            }
            Sheet sheet = workbook.getSheetAt(0);

            Epic currentEpic = null;
            Story currentStory = null;

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                // ── Epic columns: A(0) B(1) C(2) D(3) ──────────────────────
                String epicName        = getCellString(row, 0);
                String epicDesc        = getCellString(row, 1);
                String epicPriority    = getCellString(row, 2);
                String epicStatusName  = getCellString(row, 3);

                // ── Story columns: E(4) F(5) G(6) H(7) I(8) J(9) ───────────
                String storyTitle       = getCellString(row, 4);
                String storyDesc        = getCellString(row, 5);
                String storyPriority    = getCellString(row, 6);
                String storyPoints      = getCellString(row, 7);
                String acceptanceCrit   = getCellString(row, 8);
                String storyStatusName  = getCellString(row, 9);

                // ── Task columns: K(10) L(11) M(12) N(13) ───────────────────
                String taskTitle       = getCellString(row, 10);
                String taskDesc        = getCellString(row, 11);
                String taskPriority    = getCellString(row, 12);
                String taskStatusName  = getCellString(row, 13);

                // ── Create Epic ──────────────────────────────────────────────
                if (!epicName.isEmpty()) {
                    try {
                        Status epicStatus = resolveStatus(epicStatusName, statusMap, defaultStatus);
                        if (epicStatus == null) {
                            errors.add("Row " + (rowIndex + 1) + " Epic: Status '" + epicStatusName
                                    + "' not found in project. Valid: " + validStatusNames(statuses));
                            currentEpic = null;
                            currentStory = null;
                            continue;
                        }
                        Epic epic = new Epic();
                        epic.setName(epicName);
                        epic.setDescription(epicDesc.isEmpty() ? null : epicDesc);
                        epic.setStatus(epicStatus);
                        epic.setProject(project);
                        epic.setCreatedBy(userId);
                        epic.setPriority(parseEpicPriority(epicPriority));
                        currentEpic = epicRepository.save(epic);
                        epicsCreated++;
                        currentStory = null;
                    } catch (Exception e) {
                        errors.add("Row " + (rowIndex + 1) + " Epic: " + e.getMessage());
                        currentEpic = null;
                        currentStory = null;
                        continue;
                    }
                }

                // ── Create Story ─────────────────────────────────────────────
                if (!storyTitle.isEmpty()) {
                    try {
                        Status storyStatus = resolveStatus(storyStatusName, statusMap, defaultStatus);
                        if (storyStatus == null) {
                            errors.add("Row " + (rowIndex + 1) + " Story: Status '" + storyStatusName
                                    + "' not found in project. Valid: " + validStatusNames(statuses));
                            currentStory = null;
                            continue;
                        }
                        Story story = new Story();
                        story.setTitle(storyTitle);
                        story.setDescription(storyDesc.isEmpty() ? null : storyDesc);
                        story.setStatus(storyStatus);
                        story.setProject(project);
                        story.setEpic(currentEpic);
                        story.setReporterId(userId);
                        story.setCreatedBy(userId);
                        story.setPriority(parseStoryPriority(storyPriority));
                        story.setStoryPoints(parseStoryPoints(storyPoints));
                        story.setAcceptanceCriteria(acceptanceCrit.isEmpty() ? null : acceptanceCrit);
                        currentStory = storyRepository.save(story);
                        storiesCreated++;
                    } catch (Exception e) {
                        errors.add("Row " + (rowIndex + 1) + " Story: " + e.getMessage());
                        currentStory = null;
                        continue;
                    }
                }

                // ── Create Task ──────────────────────────────────────────────
                if (!taskTitle.isEmpty()) {
                    try {
                        Status taskStatus = resolveStatus(taskStatusName, statusMap, defaultStatus);
                        if (taskStatus == null) {
                            errors.add("Row " + (rowIndex + 1) + " Task: Status '" + taskStatusName
                                    + "' not found in project. Valid: " + validStatusNames(statuses));
                            continue;
                        }
                        Task task = new Task();
                        task.setTitle(taskTitle);
                        task.setDescription(taskDesc.isEmpty() ? null : taskDesc);
                        task.setStatus(taskStatus);
                        task.setProject(project);
                        task.setStory(currentStory);
                        task.setReporterId(userId);
                        task.setCreatedBy(userId);
                        task.setPriority(parseTaskPriority(taskPriority));
                        taskRepository.save(task);
                        tasksCreated++;
                    } catch (Exception e) {
                        errors.add("Row " + (rowIndex + 1) + " Task: " + e.getMessage());
                    }
                }
            }
        }

        String importStatus = errors.isEmpty() ? "SUCCESS"
                : (epicsCreated + storiesCreated + tasksCreated > 0 ? "PARTIAL" : "FAILED");

        ProjectExcelImport record = new ProjectExcelImport();
        record.setProjectId(projectId);
        record.setFileName(file.getOriginalFilename());
        record.setFileData(file.getBytes());
        record.setUploadedAt(LocalDateTime.now());
        record.setUploadedBy(userId);
        record.setStatus(importStatus);
        record.setEpicsCreated(epicsCreated);
        record.setStoriesCreated(storiesCreated);
        record.setTasksCreated(tasksCreated);
        ProjectExcelImport saved = excelImportRepository.save(record);

        ExcelImportResultDto result = new ExcelImportResultDto();
        result.setImportId(saved.getId());
        result.setProjectId(projectId);
        result.setFileName(file.getOriginalFilename());
        result.setEpicsCreated(epicsCreated);
        result.setStoriesCreated(storiesCreated);
        result.setTasksCreated(tasksCreated);
        result.setStatus(importStatus);
        result.setErrors(errors);
        result.setUploadedAt(saved.getUploadedAt());
        return result;
    }

    public List<ExcelImportResultDto> getImportsByProject(Long projectId) {
        return excelImportRepository.findByProjectIdOrderByUploadedAtDesc(projectId)
                .stream()
                .map(r -> {
                    ExcelImportResultDto dto = new ExcelImportResultDto();
                    dto.setImportId(r.getId());
                    dto.setProjectId(r.getProjectId());
                    dto.setFileName(r.getFileName());
                    dto.setEpicsCreated(r.getEpicsCreated());
                    dto.setStoriesCreated(r.getStoriesCreated());
                    dto.setTasksCreated(r.getTasksCreated());
                    dto.setStatus(r.getStatus());
                    dto.setErrors(List.of());
                    dto.setUploadedAt(r.getUploadedAt());
                    return dto;
                })
                .toList();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Resolves status by name (case-insensitive).
     * - Blank/missing → returns defaultStatus (no error)
     * - Non-blank but not found → returns null (caller adds error)
     */
    private Status resolveStatus(String name, Map<String, Status> statusMap, Status defaultStatus) {
        if (name == null || name.isBlank()) return defaultStatus;
        return statusMap.get(name.toLowerCase().trim());
    }

    private String validStatusNames(List<Status> statuses) {
        return statuses.stream().map(Status::getName).collect(Collectors.joining(", "));
    }

    private String getCellString(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    private Integer parseStoryPoints(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Integer.parseInt(value.trim()); } catch (NumberFormatException e) { return null; }
    }

    private Epic.Priority parseEpicPriority(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Epic.Priority.valueOf(value.toUpperCase()); } catch (IllegalArgumentException e) { return null; }
    }

    private Story.Priority parseStoryPriority(String value) {
        if (value == null || value.isBlank()) return Story.Priority.MEDIUM;
        try { return Story.Priority.valueOf(value.toUpperCase()); } catch (IllegalArgumentException e) { return Story.Priority.MEDIUM; }
    }

    private Task.Priority parseTaskPriority(String value) {
        if (value == null || value.isBlank()) return Task.Priority.MEDIUM;
        try { return Task.Priority.valueOf(value.toUpperCase()); } catch (IllegalArgumentException e) { return Task.Priority.MEDIUM; }
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            Object sub = jwt.getClaims().get("userId");
            if (sub instanceof Number n) return n.longValue();
            if (sub instanceof String s) return Long.parseLong(s);
        }
        return null;
    }
}
