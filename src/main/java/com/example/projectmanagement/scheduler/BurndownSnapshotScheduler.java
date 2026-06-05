package com.example.projectmanagement.scheduler;

import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.entity.graphs.SprintBurndownSnapshot;
import com.example.projectmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BurndownSnapshotScheduler {

    private final SprintRepository sprintRepository;
    private final StoryRepository storyRepository;
    private final TaskRepository taskRepository;
    private final StatusRepository statusRepository;
    private final SprintBurndownSnapshotRepository snapshotRepository;
    private final SprintScopeChangeRepository scopeChangeRepository;

    @Autowired
    @Lazy
    private BurndownSnapshotScheduler self;

    // Runs at midnight daily. Override via sprint.scheduler.cron property.
    @Scheduled(cron = "${sprint.scheduler.cron:0 0 0 * * *}")
    public void takeSnapshots() {
        LocalDate today = LocalDate.now();
        log.info("=== BurndownSnapshotScheduler FIRED for date: {} ===", today);

        List<Sprint> activeSprints = sprintRepository.findByStatus(Sprint.SprintStatus.ACTIVE);
        log.info("[BurndownScheduler] Found {} active sprint(s) to snapshot.", activeSprints.size());

        int success = 0, failed = 0;
        for (Sprint sprint : activeSprints) {
            try {
                self.takeSnapshotForSprint(sprint, today);
                log.info("[BurndownScheduler] Snapshot saved for sprint '{}' (id={})", sprint.getName(), sprint.getId());
                success++;
            } catch (Exception e) {
                log.error("[BurndownScheduler] Failed to snapshot sprint '{}' (id={}): {}", sprint.getName(), sprint.getId(), e.getMessage(), e);
                failed++;
            }
        }

        log.info("=== BurndownSnapshotScheduler DONE — success={}, failed={} ===", success, failed);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void takeSnapshotForSprint(Sprint sprint, LocalDate date) {
        // Use eager-load query so holidays + workingWeekends are fetched in the same query,
        // avoiding lazy-load failures if sprint_holidays / sprint_working_weekends tables
        // haven't been initialised in this session yet.
        final Sprint loaded = sprintRepository.findByIdWithSchedule(sprint.getId())
                .orElseThrow(() -> new RuntimeException("Sprint not found: " + sprint.getId()));

        Long projectId = loaded.getProject().getId();
        Integer doneSortOrder = statusRepository.findMaxSortOrderByProject(projectId);
        if (doneSortOrder == null) {
            log.warn("No statuses found for project {} — sprint {} snapshot will show 0 completed", projectId, loaded.getId());
        }

        // ── Story point calculations ──────────────────────────────────────

        List<Story> stories = storyRepository.findBySprintId(loaded.getId());

        int currentTotal = stories.stream()
                .mapToInt(s -> s.getStoryPoints() != null ? s.getStoryPoints() : 0)
                .sum();

        int completedPoints = stories.stream()
                .filter(s -> s.getStatus() != null
                        && Objects.equals(s.getStatus().getSortOrder(), doneSortOrder))
                .mapToInt(s -> s.getStoryPoints() != null ? s.getStoryPoints() : 0)
                .sum();

        int remainingPoints = currentTotal - completedPoints;

        // ── Issue count calculations ──────────────────────────────────────

        List<Task> tasks = taskRepository.findBySprintId(loaded.getId());

        int totalIssues = stories.size() + tasks.size();

        int completedIssues = (int) stories.stream()
                .filter(s -> s.getStatus() != null
                        && Objects.equals(s.getStatus().getSortOrder(), doneSortOrder))
                .count()
                + (int) tasks.stream()
                .filter(t -> t.getStatus() != null
                        && Objects.equals(t.getStatus().getSortOrder(), doneSortOrder))
                .count();

        int remainingIssues = totalIssues - completedIssues;

        // ── Sprint day number ─────────────────────────────────────────────

        LocalDateTime actualStart = loaded.getStartedAt() != null ? loaded.getStartedAt() : loaded.getStartDate();
        LocalDate sprintStart = actualStart.toLocalDate();
        LocalDate sprintEnd   = loaded.getEndDate().toLocalDate();

        int totalSprintDays = (int) ChronoUnit.DAYS.between(sprintStart, sprintEnd) + 1;
        int sprintDayNumber = Math.max(1, Math.min(
                (int) ChronoUnit.DAYS.between(sprintStart, date) + 1,
                totalSprintDays
        ));

        // ── Initial points — locked from day 1 snapshot ──────────────────

        int initialPoints = snapshotRepository
                .findFirstBySprintIdOrderBySnapshotDateAsc(loaded.getId())
                .map(SprintBurndownSnapshot::getInitialStoryPoints)
                .orElse(currentTotal);

        // ── Holiday / working-weekend flags ───────────────────────────────

        Set<LocalDate> holidays        = loaded.getHolidays();
        Set<LocalDate> workingWeekends = loaded.getWorkingWeekends();

        DayOfWeek dow        = date.getDayOfWeek();
        boolean isWeekend        = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
        boolean isHoliday        = holidays.contains(date);
        boolean isWorkingWeekend = workingWeekends.contains(date);

        // ── Ideal remaining (respects holidays and working weekends) ──────

        List<LocalDate> allSprintDates = sprintStart.datesUntil(sprintEnd.plusDays(1))
                .collect(Collectors.toList());

        long totalWorkingDays = allSprintDates.stream()
                .filter(d -> isWorkingDay(d, holidays, workingWeekends))
                .count();

        long workingDaysElapsed = allSprintDates.stream()
                .filter(d -> !d.isAfter(date))
                .filter(d -> isWorkingDay(d, holidays, workingWeekends))
                .count();

        int idealRemaining = (totalWorkingDays == 0) ? 0 : Math.max(0, Math.round(
                initialPoints * (1f - ((float) workingDaysElapsed / totalWorkingDays))
        ));

        // ── Yesterday's snapshot for delta calculations ───────────────────

        SprintBurndownSnapshot yesterday = snapshotRepository
                .findBySprintIdAndSnapshotDate(loaded.getId(), date.minusDays(1))
                .orElse(null);

        int prevRemaining       = yesterday != null ? yesterday.getRemainingStoryPoints() : initialPoints;
        int prevCompletedIssues = yesterday != null ? yesterday.getCompletedIssues() : 0;

        // ── Scope changes (from audit log) ────────────────────────────────

        int addedScope   = scopeChangeRepository.sumAddedPointsOnDate(loaded.getId(), date);
        int removedScope = Math.abs(scopeChangeRepository.sumRemovedPointsOnDate(loaded.getId(), date));

        // ── Velocity (burned today only) ──────────────────────────────────

        int velocityPoints = Math.max(0, prevRemaining - remainingPoints + addedScope - removedScope);
        int velocityIssues = Math.max(0, completedIssues - prevCompletedIssues);

        // ── Upsert ────────────────────────────────────────────────────────

        SprintBurndownSnapshot snapshot = snapshotRepository
                .findBySprintIdAndSnapshotDate(loaded.getId(), date)
                .orElseGet(() -> {
                    SprintBurndownSnapshot s = new SprintBurndownSnapshot();
                    s.setSprint(loaded);
                    s.setSnapshotDate(date);
                    return s;
                });

        snapshot.setSprintDayNumber(sprintDayNumber);
        snapshot.setIsWeekend(isWeekend);
        snapshot.setIsHoliday(isHoliday);
        snapshot.setIsWorkingWeekend(isWorkingWeekend);
        snapshot.setInitialStoryPoints(initialPoints);
        snapshot.setIdealRemainingPoints(idealRemaining);
        snapshot.setCurrentStoryPoints(currentTotal);
        snapshot.setCompletedStoryPoints(completedPoints);
        snapshot.setRemainingStoryPoints(remainingPoints);
        snapshot.setVelocityPoints(velocityPoints);
        snapshot.setAddedScopePoints(addedScope);
        snapshot.setRemovedScopePoints(removedScope);
        snapshot.setTotalIssues(totalIssues);
        snapshot.setCompletedIssues(completedIssues);
        snapshot.setRemainingIssues(remainingIssues);
        snapshot.setVelocityIssues(velocityIssues);

        snapshotRepository.save(snapshot);
        log.info("[BurndownScheduler] Sprint '{}' day={} | total={} remaining={} ideal={} velocity={} issues(rem={}/tot={})",
                loaded.getName(), sprintDayNumber,
                currentTotal, remainingPoints, idealRemaining, velocityPoints,
                remainingIssues, totalIssues);
    }

    private boolean isWorkingDay(LocalDate d, Set<LocalDate> holidays, Set<LocalDate> workingWeekends) {
        if (workingWeekends.contains(d)) return true;
        DayOfWeek dw = d.getDayOfWeek();
        if (dw == DayOfWeek.SATURDAY || dw == DayOfWeek.SUNDAY) return false;
        if (holidays.contains(d)) return false;
        return true;
    }
}