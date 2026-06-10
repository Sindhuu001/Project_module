package com.example.projectmanagement.service;

import com.example.projectmanagement.ExternalDTO.ProjectTasksDto.TaskDto;
import com.example.projectmanagement.client.UserClient;
import com.example.projectmanagement.dto.SprintBurndownResponse;
import com.example.projectmanagement.dto.SprintDto;
import com.example.projectmanagement.dto.SprintHolidayRequest;
import com.example.projectmanagement.dto.SprintPopupResponse;
import com.example.projectmanagement.dto.SprintScheduleResponse;
import com.example.projectmanagement.dto.UserDto;
import com.example.projectmanagement.entity.Project;
import com.example.projectmanagement.entity.Sprint;
import com.example.projectmanagement.entity.Story;
//import com.example.projectmanagement.entity.Story.StoryStatus;
import com.example.projectmanagement.entity.Task;
//import com.example.projectmanagement.entity.Task.TaskStatus;
import com.example.projectmanagement.exception.SprintCompletionException;
import com.example.projectmanagement.repository.ProjectRepository;
import com.example.projectmanagement.repository.SprintBurndownSnapshotRepository;
import com.example.projectmanagement.repository.SprintRepository;
import com.example.projectmanagement.repository.StoryRepository;
import com.example.projectmanagement.repository.TaskRepository;
import com.example.projectmanagement.scheduler.BurndownSnapshotScheduler;
import com.example.projectmanagement.repository.StatusRepository;
import com.example.projectmanagement.entity.RolePermissionChecker;
import com.example.projectmanagement.entity.Status;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class SprintService {
    @Autowired
private BurndownSnapshotScheduler burndownSnapshotScheduler;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private StoryRepository storyRepository;
    @Autowired
    private UserClient userClient;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private SprintBurndownSnapshotRepository snapshotRepository;

    @Value("${sprint.prompt.window.hours:24}")
    private int promptWindowHours;

    public SprintDto createSprint(SprintDto sprintDto, Long currentUserId) {
        UserDto currentUserDto = userService.getUserWithRoles(currentUserId);
        if (!RolePermissionChecker.canCreateSprint(currentUserDto.getRoles())) {
            throw new RuntimeException("Access denied: You are not allowed to create a sprint.");
        }

        Project project = projectRepository.findById(sprintDto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + sprintDto.getProjectId()));

        if (sprintRepository.existsByNameAndProjectId(sprintDto.getName(), sprintDto.getProjectId())) {
            throw new RuntimeException("Sprint with name '" + sprintDto.getName() + "' already exists in this project.");
        }
        if (sprintDto.getStartDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Sprint start date cannot be in the past.");
        }

        if (sprintDto.getStartDate().isAfter(sprintDto.getEndDate())||
            sprintDto.getStartDate().isEqual(sprintDto.getEndDate())) {
            throw new RuntimeException("End Date must be later than Start Date");
        }

        validateNoSprintOverlap(sprintDto.getProjectId(), sprintDto.getStartDate(), sprintDto.getEndDate(), null);

        Sprint sprint = modelMapper.map(sprintDto, Sprint.class);
        sprint.setProject(project);

        Sprint savedSprint = sprintRepository.save(sprint);
        return convertToDto(savedSprint);
    }

    public SprintDto startSprint(Long id) {
        // 1. Find the sprint
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));

        // 2. Ensure it's in PLANNED state
        if (sprint.getStatus() != Sprint.SprintStatus.PLANNING) {
            throw new IllegalStateException("Only planned sprints can be started");
        }

        // 3. Ensure the sprint is not empty
        long taskCount = taskRepository.countBySprintId(id);
        long storyCount = storyRepository.countBySprintId(id);
        if (taskCount == 0 && storyCount == 0) {
            throw new IllegalStateException("Cannot start an empty sprint. Add at least one task or story.");
        }

        // 4. Ensure no other ACTIVE sprint exists in this project
        boolean hasActiveSprint = sprintRepository.existsActiveSprintInProject(sprint.getProject().getId());
        if (hasActiveSprint) {
            throw new IllegalStateException("Another active sprint already exists in this project.");
        }

        // 5. Update sprint status and time
        sprint.setStatus(Sprint.SprintStatus.ACTIVE);
        sprint.setStartedAt(LocalDateTime.now());
        log.info("Sprint '{}' (id={}) started at {}", sprint.getName(), sprint.getId(), sprint.getStartedAt());

        // 6. Save — snapshot must run AFTER this transaction commits so that
        //    REQUIRES_NEW sees startedAt in the DB (READ COMMITTED isolation).
        Sprint updatedSprint = sprintRepository.save(sprint);
        final Long capturedId = updatedSprint.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    Sprint stub = new Sprint();
                    stub.setId(capturedId);
                    burndownSnapshotScheduler.takeSnapshotForSprint(stub, LocalDate.now());
                } catch (Exception e) {
                    log.error("Failed to take Day-1 snapshot for sprint {}: {}", capturedId, e.getMessage(), e);
                }
            }
        });
        return convertToDto(updatedSprint);
    }

//    public SprintDto completeSprint(Long id) {
//        // 1️⃣ Fetch sprint by ID
//        Sprint sprint = sprintRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));
//
//        // 2️⃣ Ensure sprint is active
//        if (sprint.getStatus() != Sprint.SprintStatus.ACTIVE) {
//            throw new RuntimeException("Only active sprints can be completed");
//        }
//
//        // 3️⃣ Determine the "Done" status for the project
//        Optional<Status> doneStatusOpt = statusRepository.findTopByProjectIdOrderBySortOrderDesc(sprint.getProject().getId());
//        if (doneStatusOpt.isEmpty()) {
//            throw new RuntimeException("Cannot complete sprint: No statuses defined for the project.");
//        }
//        Long doneStatusId = doneStatusOpt.get().getId();
//
//        // 4️⃣ Fetch all tasks in this sprint
//        List<Task> tasks = taskRepository.findBySprintId(sprint.getId());
//
//        // 5️⃣ Fetch all stories in this sprint
//        List<Story> stories = storyRepository.findBySprintId(sprint.getId());
//
//        // 6️⃣ Check for incomplete tasks
//        List<Task> incompleteTasks = tasks.stream()
//                .filter(t -> t.getStatus() == null || !t.getStatus().getId().equals(doneStatusId))
//                .toList();
//
//        // 7️⃣ Check for incomplete stories
//        List<Story> incompleteStories = stories.stream()
//                .filter(s -> s.getStatus() == null || !s.getStatus().getId().equals(doneStatusId))
//                .toList();
//
//        // 8️⃣ Throw exception if any tasks or stories are not done
//        if (!incompleteTasks.isEmpty() || !incompleteStories.isEmpty()) {
//            String taskMsg = incompleteTasks.isEmpty() ? "" : "Tasks not done: " + incompleteTasks.stream().map(Task::getTitle).toList();
//            String storyMsg = incompleteStories.isEmpty() ? "" : "Stories not done: " + incompleteStories.stream().map(Story::getTitle).toList();
//            throw new RuntimeException("Cannot complete sprint. " + taskMsg + " " + storyMsg);
//        }
//
//        // 9️⃣ Mark sprint as completed
//        sprint.setStatus(Sprint.SprintStatus.COMPLETED);
//        sprint.setEndDate(LocalDateTime.now());
//        Sprint updatedSprint = sprintRepository.save(sprint);
//
//        // 🔟 Return DTO
//        return convertToDto(updatedSprint);
//    }
public SprintDto completeSprint(Long id) {
    // 1️⃣ Fetch sprint by ID
    Sprint sprint = sprintRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));

    // 2️⃣ Ensure sprint is active
    if (sprint.getStatus() != Sprint.SprintStatus.ACTIVE) {
        throw new RuntimeException("Only active sprints can be completed");
    }

    // 3️⃣ Determine the "Done" status for the project
    Optional<Status> doneStatusOpt = statusRepository.findTopByProjectIdOrderBySortOrderDesc(sprint.getProject().getId());
    if (doneStatusOpt.isEmpty()) {
        throw new RuntimeException("Cannot complete sprint: No statuses defined for the project.");
    }
    Long doneStatusId = doneStatusOpt.get().getId();

    // 4️⃣ Fetch all tasks in this sprint
    List<Task> tasks = taskRepository.findBySprintId(sprint.getId());

    // 5️⃣ Fetch all stories in this sprint
    List<Story> stories = storyRepository.findBySprintId(sprint.getId());

    // 6️⃣ Check for incomplete tasks
    List<Task> incompleteTasks = tasks.stream()
            .filter(t -> t.getStatus() == null || !t.getStatus().getId().equals(doneStatusId))
            .toList();

    // 7️⃣ Check for incomplete stories
    List<Story> incompleteStories = stories.stream()
            .filter(s -> s.getStatus() == null || !s.getStatus().getId().equals(doneStatusId))
            .toList();

    // 8️⃣ Throw structured exception if any tasks or stories are not done
    if (!incompleteTasks.isEmpty() || !incompleteStories.isEmpty()) {
        List<String> taskNames = incompleteTasks.stream()
                .map(Task::getTitle)
                .toList();
        List<String> storyNames = incompleteStories.stream()
                .map(Story::getTitle)
                .toList();

        throw new SprintCompletionException(taskNames, storyNames);
    }

    // 9️⃣ Mark sprint as completed
    sprint.setStatus(Sprint.SprintStatus.COMPLETED);
    sprint.setEndDate(LocalDateTime.now());
    Sprint updatedSprint = sprintRepository.save(sprint);

    // 🔟 Return DTO
    return convertToDto(updatedSprint);
}



    public SprintDto updateSprint(Long id, SprintDto sprintDto) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));

        Project project = projectRepository.findById(sprintDto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + sprintDto.getProjectId()));

        if (sprintDto.getStartedAt().isAfter(sprintDto.getEndDate())) {
            throw new RuntimeException("Start date cannot be after end date");
        }

        validateNoSprintOverlap(sprintDto.getProjectId(), sprintDto.getStartDate(), sprintDto.getEndDate(), id);

        sprint.setName(sprintDto.getName());
        sprint.setGoal(sprintDto.getGoal());
        sprint.setStartDate(sprintDto.getStartDate());
        sprint.setEndDate(sprintDto.getEndDate());
        sprint.setProject(project);

        Sprint updatedSprint = sprintRepository.save(sprint);
        return convertToDto(updatedSprint);
    }

    public void deleteSprint(Long id, Long currentUserId) {
        UserDto currentUser = userService.getUserWithRoles(currentUserId);
        if (!RolePermissionChecker.canDeleteSprint(currentUser.getRoles())) {
            throw new RuntimeException("Access denied: You are not allowed to delete sprints.");
        }

        if (!sprintRepository.existsById(id)) {
            throw new RuntimeException("Sprint not found with id: " + id);
        }
        sprintRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public SprintDto getSprintById(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + id));
        return convertToDto(sprint);
    }

    @Transactional(readOnly = true)
    public List<SprintDto> getAllSprints() {
        return sprintRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<SprintDto> getAllSprints(Pageable pageable) {
        Map<Long, UserDto> userMap = userClient.findAll().stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity()));
        return sprintRepository.findAll(pageable)
                .map(sprint -> convertToDto1(sprint, userMap));
    }

    @Transactional(readOnly = true)
    public List<SprintDto> getSprintsByProject(Long projectId) {
        return sprintRepository.findByProjectId(projectId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SprintDto> getSprintsByStatus(Sprint.SprintStatus status) {
        return sprintRepository.findByStatus(status).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SprintDto> getActiveSprintsByProject(Long projectId) {
        return sprintRepository.findActiveSprintsByProject(projectId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

    }


    @Transactional(readOnly = true)
    public List<SprintDto> getOverdueSprints() {
        return sprintRepository.findOverdueSprints(LocalDateTime.now()).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private void validateNoSprintOverlap(Long projectId, LocalDateTime startDate, LocalDateTime endDate, Long excludeId) {

        List<Sprint> overlappingSprints =
                sprintRepository.findOverlappingSprints(projectId, startDate, endDate);

        overlappingSprints = overlappingSprints.stream()
                .filter(s -> s.getStatus() != Sprint.SprintStatus.COMPLETED) // ignore completed
                .filter(s -> excludeId == null || !s.getId().equals(excludeId))
                .collect(Collectors.toList());

        if (!overlappingSprints.isEmpty()) {
            throw new RuntimeException("A sprint already exists within this date range.");
        }
    }

    // ── Holiday & Working Weekend Management ─────────────────────────────

    public SprintScheduleResponse getSchedule(Long sprintId) {
        Sprint sprint = sprintRepository.findByIdWithSchedule(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprintId));
        return buildScheduleResponse(sprint);
    }

    public List<LocalDate> getHolidays(Long sprintId) {
        Sprint sprint = sprintRepository.findByIdWithSchedule(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprintId));
        return sprint.getHolidays().stream().sorted().collect(Collectors.toList());
    }

    public List<LocalDate> getWorkingWeekends(Long sprintId) {
        Sprint sprint = sprintRepository.findByIdWithSchedule(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprintId));
        return sprint.getWorkingWeekends().stream().sorted().collect(Collectors.toList());
    }

    public SprintScheduleResponse addHolidays(Long sprintId, List<LocalDate> dates) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprintId));
        sprint.getHolidays().addAll(dates);
        sprintRepository.save(sprint);
        updateSnapshotHolidayFlags(sprintId, dates, true, false);
        return buildScheduleResponse(sprint);
    }

    public SprintScheduleResponse removeHolidays(Long sprintId, List<LocalDate> dates) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprintId));
        sprint.getHolidays().removeAll(dates);
        sprintRepository.save(sprint);
        updateSnapshotHolidayFlags(sprintId, dates, false, false);
        return buildScheduleResponse(sprint);
    }

    public SprintScheduleResponse addWorkingWeekends(Long sprintId, List<LocalDate> dates) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprintId));
        sprint.getWorkingWeekends().addAll(dates);
        sprintRepository.save(sprint);
        updateSnapshotWorkingWeekendFlags(sprintId, dates, true);
        return buildScheduleResponse(sprint);
    }

    public SprintScheduleResponse removeWorkingWeekends(Long sprintId, List<LocalDate> dates) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprintId));
        sprint.getWorkingWeekends().removeAll(dates);
        sprintRepository.save(sprint);
        updateSnapshotWorkingWeekendFlags(sprintId, dates, false);
        return buildScheduleResponse(sprint);
    }

    private void updateSnapshotHolidayFlags(Long sprintId, List<LocalDate> dates, boolean isHoliday, boolean isWorkingWeekend) {
        for (LocalDate date : dates) {
            snapshotRepository.findBySprintIdAndSnapshotDate(sprintId, date).ifPresent(snap -> {
                snap.setIsHoliday(isHoliday);
                if (isHoliday) snap.setIsWorkingWeekend(false);
                snapshotRepository.save(snap);
            });
        }
    }

    private void updateSnapshotWorkingWeekendFlags(Long sprintId, List<LocalDate> dates, boolean isWorkingWeekend) {
        for (LocalDate date : dates) {
            snapshotRepository.findBySprintIdAndSnapshotDate(sprintId, date).ifPresent(snap -> {
                snap.setIsWorkingWeekend(isWorkingWeekend);
                snapshotRepository.save(snap);
            });
        }
    }

    private SprintScheduleResponse buildScheduleResponse(Sprint sprint) {
        SprintScheduleResponse r = new SprintScheduleResponse();
        r.setSprintId(sprint.getId());
        r.setSprintName(sprint.getName());
        r.setHolidays(sprint.getHolidays().stream().sorted().collect(Collectors.toList()));
        r.setWorkingWeekends(sprint.getWorkingWeekends().stream().sorted().collect(Collectors.toList()));
        return r;
    }

    public SprintDto convertToDto(Sprint sprint) {
        SprintDto dto = modelMapper.map(sprint, SprintDto.class);
        dto.setProjectId(sprint.getProject().getId());
        dto.setProjectName(sprint.getProject().getName());
        return dto;
    }

    public SprintDto convertToDto1(Sprint sprint, Map<Long, UserDto> userMap) {
        SprintDto dto = modelMapper.map(sprint, SprintDto.class);
        dto.setProjectId(sprint.getProject().getId());
        dto.setProjectName(sprint.getProject().getName());
        return dto;
    }

    /**
     * Called by frontend to check popup state for single sprint
     */
    public SprintPopupResponse checkSprintPopup(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found with id: " + sprintId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = sprint.getEndDate();

        // 1️⃣ Sprint status
        boolean isActive = sprint.getStatus() == Sprint.SprintStatus.ACTIVE;

        // 2️⃣ Check if sprint is ending soon
        boolean isEndingSoon = !now.isAfter(end) &&
                Duration.between(now, end).toHours() <= promptWindowHours;

        // 3️⃣ Determine the "done" status sort order for this project
        Integer finalSortOrder = statusRepository.findMaxSortOrderByProject(sprint.getProject().getId());

        // 4️⃣ Check for unfinished tasks in the sprint
        boolean hasUnfinishedTasks = taskRepository
                .existsTaskWithSprintIdAndStatusSortOrderNot(sprintId, finalSortOrder);

        // 5️⃣ Check for stories without any tasks (edge case)
        boolean hasStoriesWithoutTasks = storyRepository
                .existsBySprintIdWithNoTasks(sprintId);

        // 6️⃣ Combine both conditions
        boolean hasUnfinished = hasUnfinishedTasks || hasStoriesWithoutTasks;

        // 7️⃣ Decide if popup should show
        boolean shouldShowPopup = isActive && isEndingSoon && hasUnfinished;

        return new SprintPopupResponse(
                sprint.getId(),
                sprint.getName(),
                isEndingSoon,
                hasUnfinished,
                shouldShowPopup
        );
    }



    /**
     * Move incomplete tasks according to user choice and close sprint.
     * option: "NEXT_SPRINT" or "BACKLOG"
     */
   @Transactional(readOnly = true)
public SprintBurndownResponse getSprintBurndown(Long sprintId) {

    Sprint sprint = sprintRepository.findById(sprintId)
            .orElseThrow(() -> new RuntimeException("Sprint not found"));

    // Fetch all stories linked to this sprint
    List<Story> stories = storyRepository.findBySprintId(sprintId);

    // Find "Done" status
    Integer doneSortOrder = statusRepository.findMaxSortOrderByProject(
            sprint.getProject().getId()
    );

    // Total story points in Sprint
    int totalStoryPoints = stories.stream()
            .mapToInt(s -> s.getStoryPoints() != null ? s.getStoryPoints() : 0)
            .sum();

    LocalDate start = sprint.getStartDate().toLocalDate();
    LocalDate end = sprint.getEndDate().toLocalDate();

    List<SprintBurndownResponse.DailyBurn> dailyBurnList =
            start.datesUntil(end.plusDays(1))
                    .map(date -> {

                        int remaining = stories.stream()
                                .filter(story -> {

                                    Status st = story.getStatus();

                                    boolean isDone = st != null &&
                                            st.getSortOrder() == doneSortOrder;

                                    // FIX: use completedAt instead of updatedAt
                                    boolean completedBeforeDate =
                                            story.getCompletedAt() != null &&
                                            story.getCompletedAt().toLocalDate()
                                                    .isBefore(date.plusDays(1));

                                    // If done AND completed before this date → remove it
                                    return !(isDone && completedBeforeDate);
                                })
                                .mapToInt(story -> {
                                    Integer sp = story.getStoryPoints();
                                    return sp != null ? sp : 0;
                                })
                                .sum();

                        SprintBurndownResponse.DailyBurn d = new SprintBurndownResponse.DailyBurn();
                        d.setDate(date);
                        d.setRemaining(remaining);
                        return d;
                    })
                    .collect(Collectors.toList());

    SprintBurndownResponse response = new SprintBurndownResponse();
    response.setSprintId(sprintId);
    response.setStartDate(start);
    response.setEndDate(end);
    response.setTotalStoryPoints(totalStoryPoints);
    response.setDailyBurn(dailyBurnList);

    return response;
}


    @Transactional
    public void finishSprintWithOption(Long sprintId, String option) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint not found"));

        Integer finalSortOrder =
                statusRepository.findMaxSortOrderByProject(sprint.getProject().getId());

        // Fetch incomplete tasks and stories
        List<Task> incompleteTasks =
                taskRepository.findIncompleteTasksBySprintId(sprintId, finalSortOrder);

        List<Story> incompleteStories =
                storyRepository.findIncompleteStoriesBySprintId(sprintId, finalSortOrder);

        Sprint targetSprint = null;

        if ("NEXT_SPRINT".equalsIgnoreCase(option)) {
            Optional<Sprint> nextSprintOpt = sprintRepository
                    .findFirstByProject_IdAndStartDateAfterOrderByStartDateAsc(
                            sprint.getProject().getId(), sprint.getEndDate()
                    );

            targetSprint = nextSprintOpt.orElse(null);

        } else if (!"BACKLOG".equalsIgnoreCase(option)) {
            throw new IllegalArgumentException("Invalid option. Expected NEXT_SPRINT or BACKLOG");
        }

        // Move incomplete tasks
        for (Task task : incompleteTasks) {
            task.setSprint(targetSprint);
        }

        // Move incomplete stories
        for (Story story : incompleteStories) {
            story.setSprint(targetSprint);

            // Move tasks under the story too
            if (story.getTasks() != null && !story.getTasks().isEmpty()) {
                for (Task t : story.getTasks()) {
                    t.setSprint(targetSprint);
                }
            }
        }

        // Save all updates
        taskRepository.saveAll(incompleteTasks);
        storyRepository.saveAll(incompleteStories);

        // Complete sprint
        sprint.setStatus(Sprint.SprintStatus.COMPLETED);
        sprint.setEndDate(LocalDateTime.now());
        sprintRepository.save(sprint);
    }



    /**
     * Auto-process expired sprints. Called by scheduler.
     * For any ACTIVE sprint with endDate <= now, move incomplete tasks automatically:
     * - If next sprint exists -> move to next
     * - else -> move to backlog
     * Then mark sprint COMPLETED.
     */
    @Transactional
    public void processExpiredSprints() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[SprintScheduler] Running expiry check at {}", now);

        // 1️⃣ Fetch all ACTIVE expired sprints
        List<Sprint> expiredSprints =
                sprintRepository.findByStatusAndEndDateBefore(Sprint.SprintStatus.ACTIVE, now);

        if (expiredSprints.isEmpty()) {
            log.info("[SprintScheduler] No expired active sprints found — nothing to process.");
            return;
        }

        log.info("[SprintScheduler] Found {} expired sprint(s) to process.", expiredSprints.size());

        for (Sprint sprint : expiredSprints) {
            log.info("[SprintScheduler] Processing sprint '{}' (id={}, projectId={}, endDate={})",
                    sprint.getName(), sprint.getId(), sprint.getProject().getId(), sprint.getEndDate());

            Long projectId = sprint.getProject().getId();

            // 2️⃣ Find final (Done) status order for this project
            Integer finalSortOrder = statusRepository.findMaxSortOrderByProject(projectId);
            log.debug("[SprintScheduler] Done status sortOrder for project {} = {}", projectId, finalSortOrder);

            // 3️⃣ Move INCOMPLETE STORIES first (because they are higher hierarchy)
            List<Story> incompleteStoriesInSprint =
                    storyRepository.findIncompleteStoriesBySprintId(sprint.getId(), finalSortOrder);

            // Find next sprint
            Optional<Sprint> nextSprintOpt =
                    sprintRepository.findFirstByProject_IdAndStartDateAfterOrderByStartDateAsc(
                            projectId, sprint.getEndDate());

            Sprint nextSprint = nextSprintOpt.orElse(null);
            String destination = nextSprint != null
                    ? "next sprint '" + nextSprint.getName() + "' (id=" + nextSprint.getId() + ")"
                    : "backlog";

            if (!incompleteStoriesInSprint.isEmpty()) {
                log.info("[SprintScheduler] Moving {} incomplete story(ies) to {} for sprint '{}'",
                        incompleteStoriesInSprint.size(), destination, sprint.getName());
                if (nextSprint != null) {
                    incompleteStoriesInSprint.forEach(story -> story.setSprint(nextSprint));
                } else {
                    incompleteStoriesInSprint.forEach(story -> story.setSprint(null));
                }
                storyRepository.saveAll(incompleteStoriesInSprint);
            } else {
                log.info("[SprintScheduler] No incomplete stories to move for sprint '{}'", sprint.getName());
            }

            // 4️⃣ Move TASKS that belong directly to this sprint (and not Done)
            List<Task> incompleteTasks =
                    taskRepository.findIncompleteTasksBySprintId(sprint.getId(), finalSortOrder);

            if (!incompleteTasks.isEmpty()) {
                log.info("[SprintScheduler] Moving {} incomplete task(s) to {} for sprint '{}'",
                        incompleteTasks.size(), destination, sprint.getName());
                if (nextSprint != null) {
                    incompleteTasks.forEach(task -> task.setSprint(nextSprint));
                } else {
                    incompleteTasks.forEach(task -> task.setSprint(null));
                }
                taskRepository.saveAll(incompleteTasks);
            } else {
                log.info("[SprintScheduler] No incomplete tasks to move for sprint '{}'", sprint.getName());
            }

            // 5️⃣ Mark sprint as completed
            sprint.setStatus(Sprint.SprintStatus.COMPLETED);
            sprint.setUpdatedAt(now);
            sprintRepository.save(sprint);

            log.info("[SprintScheduler] Sprint '{}' (id={}) marked COMPLETED successfully.", sprint.getName(), sprint.getId());
        }

        log.info("[SprintScheduler] Expiry check complete. Processed {} sprint(s).", expiredSprints.size());
    }


    
}
