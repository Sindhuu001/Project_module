package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.ExcelImportResultDto;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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

    @Autowired @Lazy
    private ExcelImportService self;

    // ── in-memory parsed objects ────────────────────────────────────────────

    private static class ParsedEpic {
        String name, description;
        Epic.Priority priority;
        Status status;
        List<ParsedStory> stories = new ArrayList<>();
    }

    private static class ParsedStory {
        String title, description;
        Story.Priority priority;
        Integer storyPoints;
        String acceptanceCriteria;
        Status status;
        List<ParsedTask> tasks = new ArrayList<>();
    }

    private static class ParsedTask {
        String title, description;
        Task.Priority priority;
        Status status;
    }

    // ── entry point ─────────────────────────────────────────────────────────

    public ExcelImportResultDto importFromExcel(Long projectId, MultipartFile file,
                                                Authentication authentication) throws Exception {

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null ||
                (!originalFilename.toLowerCase().endsWith(".xlsx") &&
                 !originalFilename.toLowerCase().endsWith(".xls"))) {
            throw new RuntimeException("Invalid file type. Only Excel files (.xlsx or .xls) are allowed.");
        }
        if (file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found: " + projectId));

        List<Status> statuses = statusRepository.findByProjectIdOrderBySortOrder(projectId);
        if (statuses.isEmpty()) {
            throw new RuntimeException("No statuses configured for project " + projectId);
        }

        Map<String, Status> statusMap = statuses.stream()
                .collect(Collectors.toMap(
                        s -> s.getName().toLowerCase().trim(),
                        Function.identity(),
                        (a, b) -> a));
        Status defaultStatus = statuses.get(0);
        String validStatuses = statuses.stream().map(Status::getName).collect(Collectors.joining(", "));

        Long userId = extractUserId(authentication);

        // ── PHASE 1: validate everything, zero DB writes ────────────────────
        List<String> errors = new ArrayList<>();
        List<ParsedEpic> parsedEpics = new ArrayList<>();

        Set<String> epicNamesInFile = new HashSet<>();
        Map<String, Set<String>> storyTitlesPerEpic = new HashMap<>();
        Map<String, Set<String>> taskTitlesPerStory = new HashMap<>();

        ParsedEpic currentEpic = null;
        ParsedStory currentStory = null;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            if (workbook.getNumberOfSheets() > 1) {
                throw new RuntimeException("Excel file contains " + workbook.getNumberOfSheets()
                        + " sheets. Only a single sheet is allowed.");
            }

            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            if (lastRow < 1) {
                throw new RuntimeException("Excel sheet is empty. Please add data rows starting from row 2.");
            }

            for (int rowIndex = 1; rowIndex <= lastRow; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) continue;

                String epicName      = getCellString(row, 0);
                String epicDesc      = getCellString(row, 1);
                String epicPriority  = getCellString(row, 2);
                String epicStatus    = getCellString(row, 3);

                String storyTitle    = getCellString(row, 4);
                String storyDesc     = getCellString(row, 5);
                String storyPriority = getCellString(row, 6);
                String storyPoints   = getCellString(row, 7);
                String acceptCrit    = getCellString(row, 8);
                String storyStatus   = getCellString(row, 9);

                String taskTitle     = getCellString(row, 10);
                String taskDesc      = getCellString(row, 11);
                String taskPriority  = getCellString(row, 12);
                String taskStatus    = getCellString(row, 13);

                int r = rowIndex + 1;
                if (epicName.isEmpty() && storyTitle.isEmpty() && taskTitle.isEmpty()) continue;

                // ── Epic ─────────────────────────────────────────────────
                if (!epicName.isEmpty()) {
                    boolean hasError = false;

                    if (epicName.length() < 2 || epicName.length() > 100) {
                        errors.add("Row " + r + " | Epic: Name must be 2–100 characters. Found: \"" + epicName + "\"");
                        hasError = true;
                    }
                    if (!hasError && epicNamesInFile.contains(epicName.toLowerCase())) {
                        errors.add("Row " + r + " | Epic: \"" + epicName + "\" is duplicated within this file.");
                        hasError = true;
                    }
                    if (!hasError && epicRepository.existsByNameAndProjectId(epicName, projectId)) {
                        errors.add("Row " + r + " | Epic: \"" + epicName + "\" already exists in this project.");
                        hasError = true;
                    }
                    Status resolvedStatus = resolveStatus(epicStatus, statusMap, defaultStatus, validStatuses, r, "Epic", errors);
                    if (resolvedStatus == null) hasError = true;

                    if (hasError) { currentEpic = null; currentStory = null; continue; }

                    epicNamesInFile.add(epicName.toLowerCase());
                    storyTitlesPerEpic.put(epicName.toLowerCase(), new HashSet<>());

                    ParsedEpic pe = new ParsedEpic();
                    pe.name = epicName;
                    pe.description = epicDesc.isEmpty() ? null : epicDesc;
                    pe.priority = parseEpicPriority(epicPriority, r, errors);
                    pe.status = resolvedStatus;
                    parsedEpics.add(pe);
                    currentEpic = pe;
                    currentStory = null;
                }

                // ── Story ─────────────────────────────────────────────────
                if (!storyTitle.isEmpty()) {
                    boolean hasError = false;

                    if (storyTitle.length() < 2 || storyTitle.length() > 200) {
                        errors.add("Row " + r + " | Story: Title must be 2–200 characters. Found: \"" + storyTitle + "\"");
                        hasError = true;
                    }
                    if (!hasError && currentEpic == null) {
                        errors.add("Row " + r + " | Story: \"" + storyTitle + "\" has no parent Epic. Add an Epic name in column A before this row.");
                        hasError = true;
                    }
                    if (!hasError) {
                        Set<String> existing = storyTitlesPerEpic.getOrDefault(currentEpic.name.toLowerCase(), new HashSet<>());
                        if (existing.contains(storyTitle.toLowerCase())) {
                            errors.add("Row " + r + " | Story: \"" + storyTitle + "\" is duplicated under Epic \"" + currentEpic.name + "\" within this file.");
                            hasError = true;
                        }
                    }
                    Integer points = parseStoryPoints(storyPoints, r, errors);
                    Status resolvedStatus = resolveStatus(storyStatus, statusMap, defaultStatus, validStatuses, r, "Story", errors);
                    if (resolvedStatus == null) hasError = true;

                    if (hasError) { currentStory = null; continue; }

                    storyTitlesPerEpic.computeIfAbsent(currentEpic.name.toLowerCase(), k -> new HashSet<>())
                                      .add(storyTitle.toLowerCase());
                    taskTitlesPerStory.put(storyTitle.toLowerCase(), new HashSet<>());

                    ParsedStory ps = new ParsedStory();
                    ps.title = storyTitle;
                    ps.description = storyDesc.isEmpty() ? null : storyDesc;
                    ps.priority = parseStoryPriority(storyPriority);
                    ps.storyPoints = points;
                    ps.acceptanceCriteria = acceptCrit.isEmpty() ? null : acceptCrit;
                    ps.status = resolvedStatus;
                    currentEpic.stories.add(ps);
                    currentStory = ps;
                }

                // ── Task ──────────────────────────────────────────────────
                if (!taskTitle.isEmpty()) {
                    boolean hasError = false;

                    if (taskTitle.length() < 2 || taskTitle.length() > 200) {
                        errors.add("Row " + r + " | Task: Title must be 2–200 characters. Found: \"" + taskTitle + "\"");
                        hasError = true;
                    }
                    if (!hasError && currentStory == null) {
                        errors.add("Row " + r + " | Task: \"" + taskTitle + "\" has no parent Story. Add a Story title in column E before this row.");
                        hasError = true;
                    }
                    if (!hasError) {
                        Set<String> existing = taskTitlesPerStory.getOrDefault(currentStory.title.toLowerCase(), new HashSet<>());
                        if (existing.contains(taskTitle.toLowerCase())) {
                            errors.add("Row " + r + " | Task: \"" + taskTitle + "\" is duplicated under Story \"" + currentStory.title + "\" within this file.");
                            hasError = true;
                        }
                    }
                    Status resolvedStatus = resolveStatus(taskStatus, statusMap, defaultStatus, validStatuses, r, "Task", errors);
                    if (resolvedStatus == null) hasError = true;

                    if (hasError) continue;

                    taskTitlesPerStory.computeIfAbsent(currentStory.title.toLowerCase(), k -> new HashSet<>())
                                      .add(taskTitle.toLowerCase());

                    ParsedTask pt = new ParsedTask();
                    pt.title = taskTitle;
                    pt.description = taskDesc.isEmpty() ? null : taskDesc;
                    pt.priority = parseTaskPriority(taskPriority);
                    pt.status = resolvedStatus;
                    currentStory.tasks.add(pt);
                }
            }
        }

        // ── If ANY error → return FAILED immediately, nothing saved ─────────
        if (!errors.isEmpty()) {
            ExcelImportResultDto result = new ExcelImportResultDto();
            result.setProjectId(projectId);
            result.setFileName(originalFilename);
            result.setEpicsCreated(0);
            result.setStoriesCreated(0);
            result.setTasksCreated(0);
            result.setStatus("FAILED");
            result.setErrors(errors);
            result.setSkipped(List.of());
            result.setUploadedAt(LocalDateTime.now());
            return result;
        }

        if (parsedEpics.isEmpty()) {
            throw new RuntimeException("No valid data found in the Excel file. Ensure at least one Epic row is present.");
        }

        // ── PHASE 2: zero errors → save all in one transaction ──────────────
        try {
            return self.saveAll(parsedEpics, project, userId, projectId, originalFilename, file.getBytes());
        } catch (Exception ex) {
            ExcelImportResultDto failed = new ExcelImportResultDto();
            failed.setProjectId(projectId);
            failed.setFileName(originalFilename);
            failed.setEpicsCreated(0);
            failed.setStoriesCreated(0);
            failed.setTasksCreated(0);
            failed.setStatus("FAILED");
            failed.setErrors(List.of(translateDbError(ex)));
            failed.setSkipped(List.of());
            failed.setUploadedAt(LocalDateTime.now());
            return failed;
        }
    }

    // ── Phase 2: all-or-nothing transactional save ──────────────────────────

    @Transactional
    public ExcelImportResultDto saveAll(List<ParsedEpic> parsedEpics, Project project, Long userId,
                                        Long projectId, String fileName, byte[] fileBytes) {
        int epicsCreated = 0, storiesCreated = 0, tasksCreated = 0;

        for (ParsedEpic pe : parsedEpics) {
            Epic epic = new Epic();
            epic.setName(pe.name);
            epic.setDescription(truncate(pe.description, 1000));
            epic.setStatus(pe.status);
            epic.setProject(project);
            epic.setCreatedBy(userId);
            epic.setPriority(pe.priority);
            Epic savedEpic = epicRepository.save(epic);
            epicsCreated++;

            for (ParsedStory ps : pe.stories) {
                Story story = new Story();
                story.setTitle(ps.title);
                story.setDescription(truncate(ps.description, 1000));
                story.setStatus(ps.status);
                story.setProject(project);
                story.setEpic(savedEpic);
                story.setReporterId(userId);
                story.setCreatedBy(userId);
                story.setPriority(ps.priority);
                story.setStoryPoints(ps.storyPoints);
                story.setAcceptanceCriteria(truncate(ps.acceptanceCriteria, 2000));
                Story savedStory = storyRepository.save(story);
                storiesCreated++;

                for (ParsedTask pt : ps.tasks) {
                    Task task = new Task();
                    task.setTitle(pt.title);
                    task.setDescription(truncate(pt.description, 1000));
                    task.setStatus(pt.status);
                    task.setProject(project);
                    task.setStory(savedStory);
                    task.setReporterId(userId);
                    task.setCreatedBy(userId);
                    task.setPriority(pt.priority);
                    taskRepository.save(task);
                    tasksCreated++;
                }
            }
        }

        ProjectExcelImport record = new ProjectExcelImport();
        record.setProjectId(projectId);
        record.setFileName(fileName);
        record.setFileData(fileBytes);
        record.setUploadedAt(LocalDateTime.now());
        record.setUploadedBy(userId);
        record.setStatus("SUCCESS");
        record.setEpicsCreated(epicsCreated);
        record.setStoriesCreated(storiesCreated);
        record.setTasksCreated(tasksCreated);
        ProjectExcelImport saved = excelImportRepository.save(record);

        ExcelImportResultDto result = new ExcelImportResultDto();
        result.setImportId(saved.getId());
        result.setProjectId(projectId);
        result.setFileName(fileName);
        result.setEpicsCreated(epicsCreated);
        result.setStoriesCreated(storiesCreated);
        result.setTasksCreated(tasksCreated);
        result.setStatus("SUCCESS");
        result.setErrors(List.of());
        result.setSkipped(List.of());
        result.setUploadedAt(saved.getUploadedAt());
        return result;
    }

    // ── download helpers ────────────────────────────────────────────────────

    public byte[] getFileData(Long importId) {
        ProjectExcelImport record = excelImportRepository.findById(importId)
                .orElseThrow(() -> new RuntimeException("Import record not found: " + importId));
        if (record.getFileData() == null) throw new RuntimeException("No file stored for import id: " + importId);
        return record.getFileData();
    }

    public String getFileName(Long importId) {
        return excelImportRepository.findById(importId)
                .map(r -> r.getFileName() != null ? r.getFileName() : "import_" + importId + ".xlsx")
                .orElseThrow(() -> new RuntimeException("Import record not found: " + importId));
    }

    public List<ExcelImportResultDto> getImportsByProject(Long projectId) {
        return excelImportRepository.findByProjectIdOrderByUploadedAtDesc(projectId).stream()
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
                    dto.setSkipped(List.of());
                    dto.setUploadedAt(r.getUploadedAt());
                    return dto;
                }).toList();
    }

    // ── private helpers ─────────────────────────────────────────────────────

    private Status resolveStatus(String name, Map<String, Status> statusMap, Status defaultStatus,
                                  String validStatuses, int row, String entity, List<String> errors) {
        if (name == null || name.isBlank()) return defaultStatus;
        Status found = statusMap.get(name.toLowerCase().trim());
        if (found == null)
            errors.add("Row " + row + " | " + entity + ": Status \"" + name
                    + "\" does not exist in this project. Valid statuses: " + validStatuses);
        return found;
    }

    private Epic.Priority parseEpicPriority(String value, int row, List<String> errors) {
        if (value == null || value.isBlank()) return null;
        try { return Epic.Priority.valueOf(value.toUpperCase()); }
        catch (IllegalArgumentException e) {
            errors.add("Row " + row + " | Epic: Priority \"" + value + "\" is invalid. Valid: LOW, MEDIUM, HIGH, CRITICAL");
            return null;
        }
    }

    private Story.Priority parseStoryPriority(String value) {
        if (value == null || value.isBlank()) return Story.Priority.MEDIUM;
        try { return Story.Priority.valueOf(value.toUpperCase()); } catch (IllegalArgumentException e) { return Story.Priority.MEDIUM; }
    }

    private Task.Priority parseTaskPriority(String value) {
        if (value == null || value.isBlank()) return Task.Priority.MEDIUM;
        try { return Task.Priority.valueOf(value.toUpperCase()); } catch (IllegalArgumentException e) { return Task.Priority.MEDIUM; }
    }

    private Integer parseStoryPoints(String value, int row, List<String> errors) {
        if (value == null || value.isBlank()) return null;
        try {
            int pts = Integer.parseInt(value.trim());
            if (pts < 0) { errors.add("Row " + row + " | Story: Story points cannot be negative. Found: " + pts); return null; }
            return pts;
        } catch (NumberFormatException e) {
            errors.add("Row " + row + " | Story: Story points must be a number. Found: \"" + value + "\"");
            return null;
        }
    }

    private String getCellString(Row row, int colIndex) {
        Sheet sheet = row.getSheet();
        Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (cell == null) {
            for (CellRangeAddress range : sheet.getMergedRegions()) {
                if (range.isInRange(row.getRowNum(), colIndex)) {
                    Row masterRow = sheet.getRow(range.getFirstRow());
                    if (masterRow != null)
                        cell = masterRow.getCell(range.getFirstColumn(), Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    break;
                }
            }
        }
        if (cell == null) return "";

        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            try {
                FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper().createFormulaEvaluator();
                CellValue cv = evaluator.evaluate(cell);
                return switch (cv.getCellType()) {
                    case STRING  -> cv.getStringValue().trim();
                    case NUMERIC -> String.valueOf((long) cv.getNumberValue());
                    case BOOLEAN -> String.valueOf(cv.getBooleanValue());
                    default      -> "";
                };
            } catch (Exception e) {
                try { return cell.getStringCellValue().trim(); } catch (Exception ignored) { return ""; }
            }
        }
        return switch (type) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    private String translateDbError(Exception ex) {
        String msg = getRootCauseMessage(ex).toLowerCase();

        if (msg.contains("data too long")) {
            if (msg.contains("description"))
                return "One or more rows have a Description that is too long for the database. " +
                       "Please shorten descriptions in your Excel file to under 1000 characters.";
            if (msg.contains("acceptance_criteria"))
                return "One or more rows have an Acceptance Criteria that is too long. " +
                       "Please shorten it to under 2000 characters.";
            if (msg.contains("title"))
                return "One or more rows have a Title that is too long. Maximum allowed is 200 characters.";
            if (msg.contains("name"))
                return "One or more Epic names are too long. Maximum allowed is 100 characters.";
            return "One or more cells in your Excel file contain text that is too long for the database. " +
                   "Please shorten the highlighted content and try again.";
        }

        if (msg.contains("duplicate entry") || msg.contains("unique constraint")) {
            if (msg.contains("epic") || msg.contains("name"))
                return "An Epic with the same name already exists in this project. " +
                       "Please use a unique Epic name and try again.";
            if (msg.contains("stories") || msg.contains("title"))
                return "A Story with the same title already exists under one of the Epics in this project.";
            if (msg.contains("tasks"))
                return "A Task with the same title already exists under one of the Stories in this project.";
            return "Some data in your Excel file already exists in this project. " +
                   "Please check for duplicate Epic, Story, or Task names and try again.";
        }

        if (msg.contains("cannot be null") || msg.contains("may not be null") || msg.contains("not-null"))
            return "One or more required fields are missing in your Excel file. " +
                   "Please ensure Epic Name, Story Title, and Task Title columns are filled in.";

        return "The import failed due to a database error. Please check your Excel data and try again. " +
               "Details: " + getRootCauseMessage(ex);
    }

    private String getRootCauseMessage(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) cause = cause.getCause();
        return cause.getMessage() != null ? cause.getMessage() : ex.getMessage();
    }

    private String truncate(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
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
