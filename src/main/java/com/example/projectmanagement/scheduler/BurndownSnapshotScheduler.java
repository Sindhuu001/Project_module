package com.example.projectmanagement.scheduler;

import com.example.projectmanagement.entity.*;
import com.example.projectmanagement.entity.graphs.SprintBurndownSnapshot;
import com.example.projectmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

    // Runs every day at midnight
    @Scheduled(cron = "0 */3 * * * *")
    @Transactional
    public void takeSnapshots() {
        LocalDate today = LocalDate.now();
        log.info("Taking burndown snapshots for date: {}", today);

        List<Sprint> activeSprints = sprintRepository.findByStatus(Sprint.SprintStatus.ACTIVE);

        for (Sprint sprint : activeSprints) {
            try {
                takeSnapshotForSprint(sprint, today);
            } catch (Exception e) {
                // Don't let one sprint failure break all others
                log.error("Failed to take snapshot for sprint {}: {}", sprint.getId(), e.getMessage());
            }
        }

        log.info("Burndown snapshots completed. Processed {} active sprints.", activeSprints.size());
    }

    // Also callable manually from service (e.g. when sprint is started)
    @Transactional
    public void takeSnapshotForSprint(Sprint sprint, LocalDate date) {

        // Idempotent — skip if already exists
        if (snapshotRepository.existsBySprintIdAndSnapshotDate(sprint.getId(), date)) {
            log.debug("Snapshot already exists for sprint {} on {}", sprint.getId(), date);
            return;
        }

        Long projectId = sprint.getProject().getId();
        Integer doneSortOrder = statusRepository.findMaxSortOrderByProject(projectId);

        // ── Story point calculations ──────────────────────────────────────

        List<Story> stories = storyRepository.findBySprintId(sprint.getId());

        int currentTotal = stories.stream()
                .mapToInt(s -> s.getStoryPoints() != null ? s.getStoryPoints() : 0)
                .sum();

        int completedPoints = stories.stream()
                .filter(s -> s.getStatus() != null
                        && s.getStatus().getSortOrder() == doneSortOrder)
                .mapToInt(s -> s.getStoryPoints() != null ? s.getStoryPoints() : 0)
                .sum();

        int remainingPoints = currentTotal - completedPoints;

        // ── Issue count calculations ──────────────────────────────────────

        List<Task> tasks = taskRepository.findBySprintId(sprint.getId());

        int totalIssues = stories.size() + tasks.size();

        int completedIssues = (int) stories.stream()
                .filter(s -> s.getStatus() != null
                        && s.getStatus().getSortOrder() == doneSortOrder)
                .count()
                + (int) tasks.stream()
                .filter(t -> t.getStatus() != null
                        && t.getStatus().getSortOrder() == doneSortOrder)
                .count();

        int remainingIssues = totalIssues - completedIssues;

        // ── Sprint day number ─────────────────────────────────────────────

        LocalDate sprintStart = sprint.getStartDate().toLocalDate();
        LocalDate sprintEnd   = sprint.getEndDate().toLocalDate();
        int sprintDayNumber   = (int) ChronoUnit.DAYS.between(sprintStart, date) + 1;
        int totalSprintDays   = (int) ChronoUnit.DAYS.between(sprintStart, sprintEnd) + 1;

        // ── Initial points — locked from day 1 snapshot ──────────────────

        int initialPoints = snapshotRepository
                .findFirstBySprintIdOrderBySnapshotDateAsc(sprint.getId())
                .map(SprintBurndownSnapshot::getCurrentStoryPoints)
                .orElse(currentTotal);   // today IS day 1

        // ── Ideal remaining (linear from initialPoints → 0) ──────────────

        int idealRemaining = Math.round(
                initialPoints * (1f - ((float)(sprintDayNumber - 1) / (totalSprintDays - 1)))
        );

        // ── Yesterday's snapshot for delta calculations ───────────────────

        SprintBurndownSnapshot yesterday = snapshotRepository
                .findBySprintIdAndSnapshotDate(sprint.getId(), date.minusDays(1))
                .orElse(null);

        int prevRemaining       = yesterday != null ? yesterday.getRemainingStoryPoints() : initialPoints;
        int prevCompletedIssues = yesterday != null ? yesterday.getCompletedIssues() : 0;

        // ── Scope changes (from audit log) ────────────────────────────────

        int addedScope   = scopeChangeRepository.sumAddedPointsOnDate(sprint.getId(), date);
        int removedScope = Math.abs(scopeChangeRepository.sumRemovedPointsOnDate(sprint.getId(), date));

        // ── Velocity (burned today only) ──────────────────────────────────

        int velocityPoints = Math.max(0, prevRemaining - remainingPoints);
        int velocityIssues = Math.max(0, completedIssues - prevCompletedIssues);

        // ── Weekend flag ──────────────────────────────────────────────────

        DayOfWeek dow = date.getDayOfWeek();
        boolean isWeekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;

        // ── Build and save ────────────────────────────────────────────────

        SprintBurndownSnapshot snapshot = new SprintBurndownSnapshot();
        snapshot.setSprint(sprint);
        snapshot.setSnapshotDate(date);
        snapshot.setSprintDayNumber(sprintDayNumber);
        snapshot.setIsWeekend(isWeekend);
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
        log.debug("Snapshot saved for sprint {} on day {}", sprint.getId(), sprintDayNumber);
    }
}