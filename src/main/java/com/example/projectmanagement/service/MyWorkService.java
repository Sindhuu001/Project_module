package com.example.projectmanagement.service;

import com.example.projectmanagement.dto.*;
import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.entity.testing.*;
import com.example.projectmanagement.enums.BugStatus;
import com.example.projectmanagement.enums.TestRunCaseStatus;
import com.example.projectmanagement.repository.*;
//import com.example.projectmanagement.repository.testing.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MyWorkService {

    private final TaskRepository        taskRepository;
    private final StoryRepository       storyRepository;
    private final BugRepository         bugRepository;
    private final TestRunRepository     testRunRepository;
    private final TestRunCaseRepository testRunCaseRepository;
    private final TestCycleRepository   testCycleRepository;
    private final StoryService          storyService;

    // ─────────────────────────────────────────────────────────────────────────
    // Main entry point
    // ─────────────────────────────────────────────────────────────────────────

    public MyWorkResponseDto getMyWork(Long userId) {

        // 1. Fetch all active assigned items in parallel (Java streams, single DB round-trips each)
        List<Task>  tasks  = taskRepository.findByAssigneeId(userId);
        List<Story> stories = storyRepository.findByAssigneeId(userId);
        List<Bug>   bugs   = bugRepository.findByAssignedTo(userId);

        // Filter out completed/closed items for the main view
        List<Task>  activeTasks   = tasks.stream()
                .filter(t -> t.getStatus() != null && !isClosedStatus(t.getStatus().getName()))
                .collect(Collectors.toList());

        List<Story> activeStories = stories.stream()
                .filter(s -> s.getStatus() != null && !isClosedStatus(s.getStatus().getName()))
                .collect(Collectors.toList());

        List<Bug>   activeBugs   = bugs.stream()
                .filter(b -> b.getStatus() != BugStatus.CLOSED
                        && b.getStatus() != BugStatus.WON_T_FIX
                        && b.getStatus() != BugStatus.DUPLICATE
                        && b.getStatus() != BugStatus.CANNOT_REPRODUCE)
                .collect(Collectors.toList());

        // 2. Normalise to WorkItemDto
        List<WorkItemDto> allItems = new ArrayList<>();
        activeTasks.forEach(t   -> allItems.add(toWorkItem(t)));
        activeStories.forEach(s -> allItems.add(toWorkItem(s)));
        activeBugs.forEach(b    -> allItems.add(toWorkItem(b)));

        // 3. Group by project
        Map<Long, List<WorkItemDto>> byProject = allItems.stream()
                .collect(Collectors.groupingBy(WorkItemDto::getProjectId));

        List<MyWorkResponseDto.ProjectWorkGroup> groups = byProject.entrySet().stream()
                .map(entry -> buildProjectGroup(entry.getKey(),
                        // get name from first item
                        entry.getValue().get(0).getProjectName(),
                        entry.getValue()))
                .sorted(Comparator.comparingInt(g -> urgencyRank(g.getUrgencyFlag())))
                .collect(Collectors.toList());

        // 4. Test work section
        List<TestWorkItemDto> testWork = buildTestWork(userId);

        // 5. Manager items (items created by this user, assigned to others)
        List<WorkItemDto> managerItems = buildManagerItems(userId);

        // 6. Snapshot counts
        LocalDate today = LocalDate.now();
        LocalDate weekEnd = today.plusDays(7);

        long overdueCount    = allItems.stream().filter(i -> "OVERDUE".equals(i.getUrgency())).count();
        long dueTodayCount   = allItems.stream().filter(i -> "DUE_TODAY".equals(i.getUrgency())).count();
        long dueThisWeekCount= allItems.stream().filter(i -> "DUE_THIS_WEEK".equals(i.getUrgency())).count();
        long blockedCount    = allItems.stream()
                .filter(i -> i.getStatusName() != null &&
                        i.getStatusName().toLowerCase().contains("block"))
                .count();

        return MyWorkResponseDto.builder()
                .overdueCount(overdueCount)
                .dueTodayCount(dueTodayCount)
                .dueThisWeekCount(dueThisWeekCount)
                .allActiveCount(allItems.size())
                .blockedCount(blockedCount)
                .projects(groups)
                .testWork(testWork)
                .managerItems(managerItems)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Completed items (fetched lazily on demand)
    // ─────────────────────────────────────────────────────────────────────────

    public MyWorkResponseDto getMyWorkCompleted(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(30);

        List<Task>  doneTasks   = taskRepository.findByAssigneeId(userId).stream()
                .filter(t -> t.getStatus() != null && isClosedStatus(t.getStatus().getName()))
                .filter(t -> t.getUpdatedAt() != null && t.getUpdatedAt().isAfter(since))
                .collect(Collectors.toList());

        List<Story> doneStories = storyRepository.findByAssigneeId(userId).stream()
                .filter(s -> s.getStatus() != null && isClosedStatus(s.getStatus().getName()))
                .filter(s -> s.getUpdatedAt() != null && s.getUpdatedAt().isAfter(since))
                .collect(Collectors.toList());

        List<Bug>   doneBugs    = bugRepository.findByAssignedTo(userId).stream()
                .filter(b -> b.getStatus() == BugStatus.CLOSED)
                .filter(b -> b.getUpdatedAt() != null && b.getUpdatedAt().isAfter(since))
                .collect(Collectors.toList());

        List<WorkItemDto> completed = new ArrayList<>();
        doneTasks.forEach(t   -> completed.add(toWorkItem(t)));
        doneStories.forEach(s -> completed.add(toWorkItem(s)));
        doneBugs.forEach(b    -> completed.add(toWorkItem(b)));

        Map<Long, List<WorkItemDto>> byProject = completed.stream()
                .collect(Collectors.groupingBy(WorkItemDto::getProjectId));

        List<MyWorkResponseDto.ProjectWorkGroup> groups = byProject.entrySet().stream()
                .map(entry -> buildProjectGroup(entry.getKey(),
                        entry.getValue().get(0).getProjectName(),
                        entry.getValue()))
                .collect(Collectors.toList());

        return MyWorkResponseDto.builder()
                .projects(groups)
                .testWork(Collections.emptyList())
                .managerItems(Collections.emptyList())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Converters
    // ─────────────────────────────────────────────────────────────────────────

    private WorkItemDto toWorkItem(Task task) {
        String urgency = computeUrgency(task.getDueDate(), task.getStatus() != null ? task.getStatus().getName() : null);
        long daysOverdue = computeDaysOverdue(task.getDueDate());

        return WorkItemDto.builder()
                .id(task.getId())
                .type("TASK")
                .title(task.getTitle())
                .priority(task.getPriority() != null ? task.getPriority().name() : "MEDIUM")
                .statusId(task.getStatus() != null ? task.getStatus().getId() : null)
                .statusName(task.getStatus() != null ? task.getStatus().getName() : "—")
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .projectName(task.getProject() != null ? task.getProject().getName() : "—")
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .sprintName(task.getSprint() != null ? task.getSprint().getName() : null)
                .dueDate(task.getDueDate())
                .updatedAt(task.getUpdatedAt())
                .createdAt(task.getCreatedAt())
                .urgency(urgency)
                .daysOverdue(daysOverdue)
                .build();
    }

    private WorkItemDto toWorkItem(Story story) {
        String urgency = computeUrgency(story.getDueDate(),
                story.getStatus() != null ? story.getStatus().getName() : null);
        long daysOverdue = computeDaysOverdue(story.getDueDate());

        return WorkItemDto.builder()
                .id(story.getId())
                .type("STORY")
                .title(story.getTitle())
                .priority(story.getPriority() != null ? story.getPriority().name() : "MEDIUM")
                .statusId(story.getStatus() != null ? story.getStatus().getId() : null)
                .statusName(story.getStatus() != null ? story.getStatus().getName() : "—")
                .projectId(story.getProject() != null ? story.getProject().getId() : null)
                .projectName(story.getProject() != null ? story.getProject().getName() : "—")
                .sprintId(story.getSprint() != null ? story.getSprint().getId() : null)
                .sprintName(story.getSprint() != null ? story.getSprint().getName() : null)
                .dueDate(story.getDueDate())
                .updatedAt(story.getUpdatedAt())
                .createdAt(story.getCreatedAt())
                .urgency(urgency)
                .daysOverdue(daysOverdue)
                .build();
    }

    private WorkItemDto toWorkItem(Bug bug) {
        String urgency = computeUrgency(null, bug.getStatus().name());
        return WorkItemDto.builder()
                .id(bug.getId())
                .type("BUG")
                .title(bug.getTitle())
                .priority(bug.getPriority() != null ? bug.getPriority().name() : "MEDIUM")
                .statusName(bug.getStatus() != null ? bug.getStatus().getDisplayName() : "—")
                .bugStatus(bug.getStatus() != null ? bug.getStatus().name() : null)
                .projectId(bug.getProject() != null ? bug.getProject().getId() : null)
                .projectName(bug.getProject() != null ? bug.getProject().getName() : "—")
                .sprintId(bug.getSprint() != null ? bug.getSprint().getId() : null)
                .sprintName(bug.getSprint() != null ? bug.getSprint().getName() : null)
                .updatedAt(bug.getUpdatedAt())
                .createdAt(bug.getCreatedAt())
                .urgency(urgency)
                .daysOverdue(0)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Urgency computation (pure client-side logic, no DB)
    // ─────────────────────────────────────────────────────────────────────────

    private String computeUrgency(LocalDateTime dueDate, String statusName) {
        if (statusName != null && statusName.toLowerCase().contains("block")) return "BLOCKED";

        if (dueDate == null) return "FUTURE";

        LocalDate due   = dueDate.toLocalDate();
        LocalDate today = LocalDate.now();

        if (due.isBefore(today))             return "OVERDUE";
        if (due.isEqual(today))              return "DUE_TODAY";
        if (!due.isAfter(today.plusDays(7))) return "DUE_THIS_WEEK";
        return "FUTURE";
    }

    private long computeDaysOverdue(LocalDateTime dueDate) {
        if (dueDate == null) return 0;
        LocalDate due   = dueDate.toLocalDate();
        LocalDate today = LocalDate.now();
        if (due.isBefore(today)) return ChronoUnit.DAYS.between(due, today);
        return 0;
    }

    private boolean isClosedStatus(String statusName) {
        if (statusName == null) return false;
        String lower = statusName.toLowerCase();
        return lower.contains("done") || lower.contains("closed")
                || lower.contains("complete") || lower.contains("resolved");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Project group builder
    // ─────────────────────────────────────────────────────────────────────────

    private MyWorkResponseDto.ProjectWorkGroup buildProjectGroup(
            Long projectId, String projectName, List<WorkItemDto> items) {

        // Sort within group: OVERDUE → BLOCKED → DUE_TODAY → DUE_THIS_WEEK → IN_SPRINT → FUTURE
        items.sort(Comparator.comparingInt(i -> urgencyItemRank(i.getUrgency())));

        long overdue  = items.stream().filter(i -> "OVERDUE".equals(i.getUrgency())).count();
        long dueToday = items.stream().filter(i -> "DUE_TODAY".equals(i.getUrgency())).count();

        String flag = overdue > 0 ? "OVERDUE" : dueToday > 0 ? "DUE_TODAY" : "NONE";

        return MyWorkResponseDto.ProjectWorkGroup.builder()
                .projectId(projectId)
                .projectName(projectName)
                .urgencyFlag(flag)
                .overdueCount((int) overdue)
                .dueTodayCount((int) dueToday)
                .items(items)
                .build();
    }

    private int urgencyRank(String flag) {
        return switch (flag) {
            case "OVERDUE"      -> 0;
            case "DUE_TODAY"    -> 1;
            case "DUE_THIS_WEEK"-> 2;
            default             -> 3;
        };
    }

    private int urgencyItemRank(String urgency) {
        return switch (urgency) {
            case "OVERDUE"       -> 0;
            case "BLOCKED"       -> 1;
            case "DUE_TODAY"     -> 2;
            case "DUE_THIS_WEEK" -> 3;
            default              -> 4;
        };
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Test work
    // ─────────────────────────────────────────────────────────────────────────

    private List<TestWorkItemDto> buildTestWork(Long userId) {
        List<TestWorkItemDto> result = new ArrayList<>();

        // Test runs created by user that are not completed
        List<TestRun> runs = testRunRepository.findByCreatedByAndStatusNot(
                userId, com.example.projectmanagement.enums.TestRunStatus.COMPLETED);

        for (TestRun run : runs) {
            List<TestRunCase> cases = testRunCaseRepository.findByRunId(run.getId());
            long remaining = cases.stream()
                    .filter(c -> c.getStatus() == com.example.projectmanagement.enums.TestRunCaseStatus.NOT_STARTED)
                    .count();

            String cycleName = run.getCycle() != null ? run.getCycle().getName() : null;
            String projectName = run.getCycle() != null && run.getCycle().getProject() != null
                    ? run.getCycle().getProject().getName() : "—";
            Long projectId = run.getCycle() != null && run.getCycle().getProject() != null
                    ? run.getCycle().getProject().getId() : null;

            result.add(TestWorkItemDto.builder()
                    .id(run.getId())
                    .type("TEST_RUN")
                    .title(run.getName())
                    .status(run.getStatus().name())
                    .projectId(projectId)
                    .projectName(projectName)
                    .cycleName(cycleName)
                    .totalCases(cases.size())
                    .remainingCases((int) remaining)
                    .createdAt(run.getCreatedAt())
                    .build());
        }

        // Test run cases assigned to user that are not done
        List<TestRunCase> assignedCases = testRunCaseRepository.findByAssigneeIdAndStatusNot(
                userId, TestRunCaseStatus.PASSED);

        for (TestRunCase rc : assignedCases) {
            String runName    = rc.getRun() != null ? rc.getRun().getName() : null;
            String cycleName  = rc.getRun() != null && rc.getRun().getCycle() != null
                    ? rc.getRun().getCycle().getName() : null;
            String caseTitle  = rc.getTestCase() != null ? rc.getTestCase().getTitle() : "Test Case";
            Long projectId    = rc.getRun() != null && rc.getRun().getCycle() != null
                    && rc.getRun().getCycle().getProject() != null
                    ? rc.getRun().getCycle().getProject().getId() : null;
            String projectName= rc.getRun() != null && rc.getRun().getCycle() != null
                    && rc.getRun().getCycle().getProject() != null
                    ? rc.getRun().getCycle().getProject().getName() : "—";

            result.add(TestWorkItemDto.builder()
                    .id(rc.getId())
                    .type("TEST_CASE")
                    .title(caseTitle)
                    .status(rc.getStatus().name())
                    .projectId(projectId)
                    .projectName(projectName)
                    .runId(rc.getRun() != null ? rc.getRun().getId() : null)
                    .runName(runName)
                    .cycleName(cycleName)
                    .lastExecutedAt(rc.getLastExecutedAt())
                    .build());
        }

        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Manager accountability items
    // ─────────────────────────────────────────────────────────────────────────

    private List<WorkItemDto> buildManagerItems(Long userId) {
        List<WorkItemDto> result = new ArrayList<>();

        // Stories created by this user assigned to someone else
        storyRepository.findByReporterId(userId).stream()
                .filter(s -> s.getAssigneeId() != null && !s.getAssigneeId().equals(userId))
                .filter(s -> s.getStatus() != null && !isClosedStatus(s.getStatus().getName()))
                .forEach(s -> result.add(toWorkItem(s)));

        // Tasks created by this user assigned to someone else
        taskRepository.findByReporterId(userId).stream()
                .filter(t -> t.getAssigneeId() != null && !t.getAssigneeId().equals(userId))
                .filter(t -> t.getStatus() != null && !isClosedStatus(t.getStatus().getName()))
                .forEach(t -> result.add(toWorkItem(t)));

        return result;
    }
}